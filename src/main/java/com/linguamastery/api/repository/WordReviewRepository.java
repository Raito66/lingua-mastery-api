package com.linguamastery.api.repository;

import com.linguamastery.api.model.WordReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordReviewRepository extends JpaRepository<WordReview, Long> {

    Optional<WordReview> findByUserIdAndWordId(Long userId, Long wordId);

    /** 取得某單字本中今日到期的複習單字（JOIN FETCH 避免 N+1，ORDER BY 最久未複習優先） */
    @Query("SELECT wr FROM WordReview wr JOIN FETCH wr.word WHERE wr.user.id = :userId AND wr.word.book.id = :bookId AND wr.nextReviewAt <= :today ORDER BY wr.nextReviewAt ASC")
    List<WordReview> findDueByUserIdAndBookId(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("today") LocalDate today,
            Pageable pageable);

    /** 統計每本單字書今日到期數，回傳 [bookId, count] 陣列 */
    @Query("SELECT wr.word.book.id, COUNT(wr) FROM WordReview wr WHERE wr.user.id = :userId AND wr.nextReviewAt <= :today GROUP BY wr.word.book.id")
    List<Object[]> countDueByBookForUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Modifying
    @Query("DELETE FROM WordReview wr WHERE wr.word.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
