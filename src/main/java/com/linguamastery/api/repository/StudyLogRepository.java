package com.linguamastery.api.repository;

import com.linguamastery.api.model.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    @Query("SELECT COUNT(sl) FROM StudyLog sl WHERE sl.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(sl) FROM StudyLog sl WHERE sl.user.id = :userId AND sl.correct = true")
    long countCorrectByUserId(@Param("userId") Long userId);
}
