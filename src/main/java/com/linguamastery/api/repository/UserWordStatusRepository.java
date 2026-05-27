package com.linguamastery.api.repository;

import com.linguamastery.api.model.UserWordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserWordStatusRepository extends JpaRepository<UserWordStatus, Long> {

    Optional<UserWordStatus> findByUserIdAndWordId(Long userId, Long wordId);

    /** 批次取得某書所有單字的熟練度，避免 N+1 查詢（JOIN FETCH word 避免 LAZY 代理觸發額外 SELECT） */
    @Query("SELECT s FROM UserWordStatus s JOIN FETCH s.word WHERE s.user.id = :userId AND s.word.book.id = :bookId")
    List<UserWordStatus> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /** 回傳 [level, count] 陣列，用於計算每本書的熟練度分布 */
    @Query("SELECT s.level, COUNT(s) FROM UserWordStatus s WHERE s.user.id = :userId AND s.word.book.id = :bookId GROUP BY s.level")
    List<Object[]> countByLevelForUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
