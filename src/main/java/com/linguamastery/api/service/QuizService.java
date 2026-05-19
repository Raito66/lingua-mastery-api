package com.linguamastery.api.service;

import com.linguamastery.api.dto.QuizQuestion;
import com.linguamastery.api.model.User;
import com.linguamastery.api.model.Word;
import com.linguamastery.api.model.WordBook;
import com.linguamastery.api.repository.UserRepository;
import com.linguamastery.api.repository.WordBookRepository;
import com.linguamastery.api.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final int QUIZ_SIZE = 10;
    private static final int MIN_OPTIONS = 2; // 至少要有 2 個選項才算有效題目

    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<QuizQuestion> getQuizQuestions(String email, Long bookId) {
        User user = getUser(email);
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }

        List<Word> quizWords = wordRepository.findRandomWordsByBookId(bookId, QUIZ_SIZE);
        if (quizWords.isEmpty()) {
            return List.of();
        }

        List<Word> allBookWords = wordRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        return quizWords.stream()
                .map(word -> buildQuestion(word, allBookWords, user.getId(), bookId))
                .filter(q -> q.getOptions().size() >= MIN_OPTIONS) // 過濾選項不足的題目
                .toList();
    }

    private QuizQuestion buildQuestion(Word correct, List<Word> allBookWords, Long userId, Long bookId) {
        // 過濾掉正確答案，收集錯誤選項池
        List<String> pool = allBookWords.stream()
                .filter(w -> !w.getId().equals(correct.getId()))
                .map(Word::getTranslation)
                .collect(Collectors.toCollection(ArrayList::new));

        // Fisher-Yates shuffle（使用 ThreadLocalRandom，thread-safe）
        for (int i = pool.size() - 1; i > 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            String tmp = pool.get(i);
            pool.set(i, pool.get(j));
            pool.set(j, tmp);
        }

        // 取前 3 個錯誤選項
        List<String> wrongOptions = new ArrayList<>(pool.subList(0, Math.min(3, pool.size())));

        // 不足 3 個時從其他書補
        if (wrongOptions.size() < 3) {
            int needed = 3 - wrongOptions.size();
            wordRepository.findRandomWordsFromOtherBooks(userId, bookId, (long) needed)
                    .stream().map(Word::getTranslation).forEach(wrongOptions::add);
        }

        // 將正確答案放在 index 0，然後用 Fisher-Yates 追蹤其位置
        // 避免 indexOf() 在翻譯重複時找到錯誤位置
        List<String> options = new ArrayList<>(wrongOptions);
        options.add(0, correct.getTranslation());

        int correctIndex = 0;
        for (int i = options.size() - 1; i > 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            String tmp = options.get(i);
            options.set(i, options.get(j));
            options.set(j, tmp);
            if (correctIndex == i) correctIndex = j;
            else if (correctIndex == j) correctIndex = i;
        }

        return new QuizQuestion(
                correct.getId(),
                correct.getWord(),
                correct.getReading(),
                correct.getLanguage().name(),
                options,
                correctIndex
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
    }
}
