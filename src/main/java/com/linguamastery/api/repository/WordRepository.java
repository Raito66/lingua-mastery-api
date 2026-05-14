package com.linguamastery.api.repository;

import com.linguamastery.api.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByBookIdOrderByCreatedAtDesc(Long bookId);
    long countByBookId(Long bookId);

    @Query(value = "SELECT * FROM words WHERE book_id = :bookId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWordsByBookId(@Param("bookId") Long bookId, @Param("limit") int limit);
}
