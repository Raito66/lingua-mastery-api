package com.linguamastery.api.service;

import com.linguamastery.api.dto.StudyResultRequest;
import com.linguamastery.api.dto.WordResponse;
import com.linguamastery.api.model.StudyLog;
import com.linguamastery.api.model.User;
import com.linguamastery.api.model.Word;
import com.linguamastery.api.model.WordBook;
import com.linguamastery.api.repository.StudyLogRepository;
import com.linguamastery.api.repository.UserRepository;
import com.linguamastery.api.repository.WordBookRepository;
import com.linguamastery.api.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private static final int STUDY_BATCH_SIZE = 10;

    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final UserRepository userRepository;
    private final StudyLogRepository studyLogRepository;
    private final StatsService statsService;

    @Transactional(readOnly = true)
    public List<WordResponse> getStudyWords(String email, Long bookId) {
        User user = getUser(email);
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }

        return wordRepository.findRandomWordsByBookId(bookId, STUDY_BATCH_SIZE)
                .stream()
                .map(this::toWordResponse)
                .toList();
    }

    @Transactional
    public void recordResult(String email, StudyResultRequest request) {
        User user = getUser(email);
        Word word = wordRepository.findById(request.getWordId())
                .orElseThrow(() -> new IllegalArgumentException("單字不存在"));

        if (!word.getBook().getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }

        StudyLog log = new StudyLog();
        log.setUser(user);
        log.setWord(word);
        log.setCorrect(request.getCorrect());
        studyLogRepository.save(log);

        statsService.recordDailyActivity(user);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
    }

    private WordResponse toWordResponse(Word word) {
        WordResponse response = new WordResponse();
        response.setId(word.getId());
        response.setWord(word.getWord());
        response.setReading(word.getReading());
        response.setTranslation(word.getTranslation());
        response.setExample(word.getExample());
        response.setLevel(word.getLevel());
        response.setLanguage(word.getLanguage());
        response.setCreatedAt(word.getCreatedAt());
        return response;
    }
}
