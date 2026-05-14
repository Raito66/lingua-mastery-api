package com.linguamastery.api.service;

import com.linguamastery.api.dto.StatsResponse;
import com.linguamastery.api.model.User;
import com.linguamastery.api.repository.StudyLogRepository;
import com.linguamastery.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudyLogRepository studyLogRepository;
    private final UserRepository userRepository;

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
}
