package com.linguamastery.api.service;

import com.linguamastery.api.dto.StatsResponse;
import com.linguamastery.api.dto.StreakResponse;
import com.linguamastery.api.model.DailyRecord;
import com.linguamastery.api.model.User;
import com.linguamastery.api.repository.DailyRecordRepository;
import com.linguamastery.api.repository.StudyLogRepository;
import com.linguamastery.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudyLogRepository studyLogRepository;
    private final UserRepository userRepository;
    private final DailyRecordRepository dailyRecordRepository;

    @Transactional(readOnly = true)
    public StatsResponse getStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));

        long total = studyLogRepository.countByUserId(user.getId());
        long correct = studyLogRepository.countCorrectByUserId(user.getId());
        double accuracy = total > 0 ? Math.round((double) correct / total * 10000.0) / 100.0 : 0.0;

        StatsResponse stats = new StatsResponse();
        stats.setTotalStudied(total);
        stats.setTotalCorrect(correct);
        stats.setAccuracy(accuracy);
        return stats;
    }

    @Transactional(readOnly = true)
    public StreakResponse getStreak(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));

        int todayCount = dailyRecordRepository
                .findByUserIdAndDate(user.getId(), LocalDate.now())
                .map(DailyRecord::getWordsStudied)
                .orElse(0);

        List<DailyRecord> records = dailyRecordRepository.findByUserIdOrderByDateDesc(user.getId());

        int streak = 0;
        // 今天還沒練習時，從昨天開始算連續天數，避免昨天以前的紀錄被清零
        LocalDate expected = todayCount > 0 ? LocalDate.now() : LocalDate.now().minusDays(1);
        for (DailyRecord record : records) {
            if (record.getDate().equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (record.getDate().isBefore(expected)) {
                break;
            }
        }

        return new StreakResponse(streak, todayCount);
    }

    /** StudyService / ReviewService 呼叫，記錄今日學習（原子性 upsert，並發安全） */
    @Transactional
    public void recordDailyActivity(User user) {
        dailyRecordRepository.upsertDailyRecord(user.getId(), LocalDate.now());
    }
}
