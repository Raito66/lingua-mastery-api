package com.linguamastery.api.service;

import com.linguamastery.api.dto.WordBookRequest;
import com.linguamastery.api.dto.WordBookResponse;
import com.linguamastery.api.model.User;
import com.linguamastery.api.model.WordBook;
import com.linguamastery.api.repository.StudyLogRepository;
import com.linguamastery.api.repository.UserRepository;
import com.linguamastery.api.repository.WordBookRepository;
import com.linguamastery.api.repository.WordRepository;
import com.linguamastery.api.repository.WordReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordBookService {

    private final WordBookRepository wordBookRepository;
    private final WordRepository wordRepository;
    private final WordReviewRepository wordReviewRepository;
    private final StudyLogRepository studyLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WordBookResponse> getBooks(String email) {
        User user = getUser(email);
        return wordBookRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(book -> toResponse(book, wordRepository.countByBookId(book.getId())))
                .toList();
    }

    @Transactional
    public WordBookResponse createBook(String email, WordBookRequest request) {
        User user = getUser(email);

        WordBook book = new WordBook();
        book.setUser(user);
        book.setName(request.getName());
        book.setLanguage(request.getLanguage());
        wordBookRepository.save(book);

        return toResponse(book, 0);
    }

    @Transactional
    public WordBookResponse updateBook(String email, Long bookId, WordBookRequest request) {
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));

        if (!book.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("無權限操作此資源");
        }

        book.setName(request.getName());
        book.setLanguage(request.getLanguage());

        return toResponse(book, wordRepository.countByBookId(book.getId()));
    }

    @Transactional
    public void deleteBook(String email, Long bookId) {
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));

        if (!book.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("無權限操作此資源");
        }

        // 依外鍵順序刪除：study_logs → word_reviews → words → word_books
        studyLogRepository.deleteByBookId(bookId);
        wordReviewRepository.deleteByBookId(bookId);
        wordRepository.deleteByBookId(bookId);
        wordBookRepository.delete(book);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
    }

    private WordBookResponse toResponse(WordBook book, long wordCount) {
        WordBookResponse response = new WordBookResponse();
        response.setId(book.getId());
        response.setName(book.getName());
        response.setLanguage(book.getLanguage());
        response.setWordCount(wordCount);
        response.setCreatedAt(book.getCreatedAt());
        return response;
    }
}
