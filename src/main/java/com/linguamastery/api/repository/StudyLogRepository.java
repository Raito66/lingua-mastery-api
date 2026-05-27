package com.linguamastery.api.repository;

import com.linguamastery.api.model.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    @Query("SELECT COUNT(sl) FROM StudyLog sl WHERE sl.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(sl) FROM StudyLog sl WHERE sl.user.id = :userId AND sl.correct = true")
    long countCorrectByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(sl) FROM StudyLog sl WHERE sl.user.id = :userId AND sl.word.book.id = :bookId")
    long countByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Query("SELECT COUNT(sl) FROM StudyLog sl WHERE sl.user.id = :userId AND sl.word.book.id = :bookId AND sl.correct = true")
    long countCorrectByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Modifying
    @Query("DELETE FROM StudyLog sl WHERE sl.word.book.id = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}
