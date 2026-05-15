package com.linguamastery.api.service;

import com.linguamastery.api.dto.BookReviewStats;
import com.linguamastery.api.dto.WordResponse;
import com.linguamastery.api.model.User;
import com.linguamastery.api.model.Word;
import com.linguamastery.api.model.WordBook;
import com.linguamastery.api.model.WordReview;
import com.linguamastery.api.repository.UserRepository;
import com.linguamastery.api.repository.WordBookRepository;
import com.linguamastery.api.repository.WordRepository;
import com.linguamastery.api.repository.WordReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_DUE_PER_SESSION = 20;
    private static final int MAX_NEW_PER_SESSION = 5;

    private final WordReviewRepository reviewRepository;
    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final UserRepository userRepository;

    /** 取得本次複習單字（到期複習 + 新單字） */
    @Transactional(readOnly = true)
    public List<WordResponse> getReviewWords(String email, Long bookId) {
        User user = getUser(email);
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));

        if (!book.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("無權限操作此資源");
        }

        // 先取到期的複習單字
        List<WordReview> dueReviews = reviewRepository.findDueByUserIdAndBookId(
                user.getId(), bookId, LocalDate.now(),
                PageRequest.of(0, MAX_DUE_PER_SESSION));

        List<WordResponse> result = new ArrayList<>();
        dueReviews.stream().map(wr -> toWordResponse(wr.getWord())).forEach(result::add);

        // 剩餘名額填入新單字
        int newSlots = Math.min(MAX_NEW_PER_SESSION, MAX_DUE_PER_SESSION - result.size());
        if (newSlots > 0) {
            wordRepository.findNewWordsForReview(bookId, user.getId(), PageRequest.of(0, newSlots))
                    .stream().map(this::toWordResponse).forEach(result::add);
        }

        return result;
    }

    /** 提交複習結果，依 SM-2 演算法更新排程 */
    @Transactional
    public void submitReview(String email, Long wordId, boolean correct) {
        User user = getUser(email);
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("單字不存在"));

        if (!word.getBook().getUser().getEmail().equals(email)) {
            throw new IllegalStateException("無權限操作此資源");
        }

        WordReview review = reviewRepository.findByUserIdAndWordId(user.getId(), wordId)
                .orElseGet(() -> {
                    WordReview r = new WordReview();
                    r.setUser(user);
                    r.setWord(word);
                    r.setNextReviewAt(LocalDate.now());
                    return r;
                });

        applySM2(review, correct);
        reviewRepository.save(review);
    }

    /** 取得所有單字本的今日複習統計 */
    @Transactional(readOnly = true)
    public List<BookReviewStats> getReviewStats(String email) {
        User user = getUser(email);
        List<WordBook> books = wordBookRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<Long> bookIds = books.stream().map(WordBook::getId).collect(Collectors.toList());

        if (bookIds.isEmpty()) {
            return List.of();
        }

        // 批次取得到期數與新單字數，各只打一次 DB
        Map<Long, Long> dueMap = new HashMap<>();
        reviewRepository.countDueByBookForUser(user.getId(), LocalDate.now())
                .forEach(row -> dueMap.put((Long) row[0], (Long) row[1]));

        Map<Long, Long> newMap = new HashMap<>();
        wordRepository.countNewWordsByBookIdsForUser(bookIds, user.getId())
                .forEach(row -> newMap.put((Long) row[0], (Long) row[1]));

        return books.stream()
                .map(book -> {
                    long dueCount = dueMap.getOrDefault(book.getId(), 0L);
                    long newCount = Math.min(newMap.getOrDefault(book.getId(), 0L), MAX_NEW_PER_SESSION);
                    return new BookReviewStats(book.getId(), book.getName(), dueCount, newCount);
                })
                .filter(stats -> stats.getDueCount() > 0 || stats.getNewCount() > 0)
                .toList();
    }

    // ── SM-2 演算法 ──────────────────────────────────────────────────────────

    private void applySM2(WordReview review, boolean correct) {
        if (correct) {
            int nextInterval;
            if (review.getRepetitions() == 0) {
                nextInterval = 1;
            } else if (review.getRepetitions() == 1) {
                nextInterval = 6;
            } else {
                nextInterval = (int) Math.round(review.getInterval() * review.getEaseFactor());
                // EaseFactor 只在 repetitions >= 2 時調整（符合 SM-2 標準規格）
                review.setEaseFactor(Math.max(1.3, review.getEaseFactor() + 0.1));
            }
            review.setInterval(nextInterval);
            review.setRepetitions(review.getRepetitions() + 1);
        } else {
            review.setInterval(1);
            review.setRepetitions(0);
            review.setEaseFactor(Math.max(1.3, review.getEaseFactor() - 0.2));
        }

        review.setNextReviewAt(LocalDate.now().plusDays(review.getInterval()));
        review.setLastReviewedAt(LocalDateTime.now());
    }

    // ── 共用 ──────────────────────────────────────────────────────────────────

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
