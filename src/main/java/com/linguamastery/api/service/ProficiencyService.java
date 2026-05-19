package com.linguamastery.api.service;

import com.linguamastery.api.model.User;
import com.linguamastery.api.model.UserWordStatus;
import com.linguamastery.api.model.Word;
import com.linguamastery.api.repository.UserWordStatusRepository;
import com.linguamastery.api.repository.WordReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProficiencyService {

    private static final int STREAK_FOR_FAMILIAR = 3;  // 連續答對幾次升為「已熟悉」
    private static final int INTERVAL_FOR_MASTERED = 21; // SRS 間隔幾天升為「已精通」

    private final UserWordStatusRepository statusRepository;
    private final WordReviewRepository reviewRepository;

    // 自注入 proxy，使 doUpdate 的 REQUIRES_NEW 能被 Spring AOP 攔截
    @Lazy
    @Autowired
    private ProficiencyService self;

    public void updateStatus(User user, Word word, boolean correct) {
        try {
            self.doUpdate(user, word, correct);
        } catch (DataIntegrityViolationException e) {
            // 並發 INSERT 衝突：舊 transaction 已結束，REQUIRES_NEW 開新 transaction 重試
            self.doUpdate(user, word, correct);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doUpdate(User user, Word word, boolean correct) {
        UserWordStatus status = statusRepository
                .findByUserIdAndWordId(user.getId(), word.getId())
                .orElseGet(() -> {
                    UserWordStatus s = new UserWordStatus();
                    s.setUser(user);
                    s.setWord(word);
                    return s;
                });

        int originalLevel = status.getLevel();

        if (correct) {
            status.setCorrectStreak(status.getCorrectStreak() + 1);

            // 第一次答對：未學習 → 學習中
            if (originalLevel == 0) {
                status.setLevel(1);
            }
            // 連續答對 3 次：學習中 → 已熟悉
            if (status.getCorrectStreak() >= STREAK_FOR_FAMILIAR && status.getLevel() < 2) {
                status.setLevel(2);
            }
            // SRS 間隔超過 21 天：已熟悉 → 已精通
            // 注意：只在「本次答題前已是已熟悉」時才檢查，避免同一次答題從 1 跳到 3
            if (originalLevel >= 2) {
                reviewRepository.findByUserIdAndWordId(user.getId(), word.getId())
                        .ifPresent(review -> {
                            if (review.getInterval() >= INTERVAL_FOR_MASTERED) {
                                status.setLevel(3);
                            }
                        });
            }
        } else {
            status.setCorrectStreak(0);
            if (originalLevel == 0) {
                // 第一次答錯：仍標記為學習中
                status.setLevel(1);
            } else if (originalLevel >= 2) {
                // 已熟悉/已精通答錯 → 退回學習中
                status.setLevel(1);
            }
        }

        statusRepository.save(status);
    }
}
