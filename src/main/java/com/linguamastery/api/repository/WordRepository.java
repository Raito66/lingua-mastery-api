package com.linguamastery.api.repository;

import com.linguamastery.api.model.Word;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByBookIdOrderByCreatedAtDesc(Long bookId);
    long countByBookId(Long bookId);
    boolean existsByBookIdAndWord(Long bookId, String word);

    @Query(value = "SELECT * FROM words WHERE book_id = :bookId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWordsByBookId(@Param("bookId") Long bookId, @Param("limit") int limit);

    /** 取得尚未加入 SRS 的新單字（使用者從未複習過） */
    @Query("SELECT w FROM Word w WHERE w.book.id = :bookId AND w.id NOT IN (SELECT wr.word.id FROM WordReview wr WHERE wr.user.id = :userId)")
    List<Word> findNewWordsForReview(@Param("bookId") Long bookId, @Param("userId") Long userId, Pageable pageable);

    /** 計算尚未加入 SRS 的新單字數量（單一書本，僅在單本統計時使用） */
    @Query("SELECT COUNT(w) FROM Word w WHERE w.book.id = :bookId AND w.id NOT IN (SELECT wr.word.id FROM WordReview wr WHERE wr.user.id = :userId)")
    long countNewWordsForReview(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /** 批次統計多本書的新單字數量，避免 N×Query（回傳 [bookId, count] 陣列） */
    @Query("SELECT w.book.id, COUNT(w) FROM Word w WHERE w.book.id IN :bookIds AND w.id NOT IN (SELECT wr.word.id FROM WordReview wr WHERE wr.user.id = :userId) GROUP BY w.book.id")
    List<Object[]> countNewWordsByBookIdsForUser(@Param("bookIds") List<Long> bookIds, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Word w WHERE w.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
