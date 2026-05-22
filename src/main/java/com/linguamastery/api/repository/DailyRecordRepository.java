package com.linguamastery.api.repository;

import com.linguamastery.api.model.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {

    Optional<DailyRecord> findByUserIdAndDate(Long userId, LocalDate date);

    /** 取得最近 730 天的學習記錄，用於計算 Streak */
    @Query("SELECT dr FROM DailyRecord dr WHERE dr.user.id = :userId ORDER BY dr.date DESC LIMIT 730")
    List<DailyRecord> findByUserIdOrderByDateDesc(@Param("userId") Long userId);

    /** 原子性遞增今日學習次數，並發安全（PostgreSQL ON CONFLICT DO UPDATE） */
    @Modifying
    @Query(value = "INSERT INTO daily_records (user_id, date, words_studied) " +
                   "VALUES (:userId, :date, 1) " +
                   "ON CONFLICT (user_id, date) DO UPDATE SET words_studied = daily_records.words_studied + 1",
           nativeQuery = true)
    void upsertDailyRecord(@Param("userId") Long userId, @Param("date") LocalDate date);
}
