package com.linguamastery.api.service;

import com.linguamastery.api.dto.WordRequest;
import com.linguamastery.api.dto.WordResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WordResponse> getWords(String email, Long bookId) {
        WordBook book = getBookForUser(email, bookId);
        return wordRepository.findByBookIdOrderByCreatedAtDesc(book.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WordResponse addWord(String email, Long bookId, WordRequest request) {
        WordBook book = getBookForUser(email, bookId);

        Word word = new Word();
        word.setBook(book);
        applyRequest(word, request);

        return toResponse(wordRepository.save(word));
    }

    @Transactional
    public WordResponse updateWord(String email, Long wordId, WordRequest request) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("單字不存在"));

        validateOwnership(email, word);
        applyRequest(word, request);

        return toResponse(wordRepository.save(word));
    }

    @Transactional
    public void deleteWord(String email, Long wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("單字不存在"));

        validateOwnership(email, word);
        wordRepository.delete(word);
    }

    private void applyRequest(Word word, WordRequest request) {
        word.setWord(request.getWord());
        word.setReading(request.getReading());
        word.setTranslation(request.getTranslation());
        word.setExample(request.getExample());
        word.setLevel(request.getLevel());
        word.setLanguage(request.getLanguage());
    }

    private WordBook getBookForUser(String email, Long bookId) {
        User user = getUser(email);
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }
        return book;
    }

    private void validateOwnership(String email, Word word) {
        User user = getUser(email);
        if (!word.getBook().getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
    }

    private WordResponse toResponse(Word word) {
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
