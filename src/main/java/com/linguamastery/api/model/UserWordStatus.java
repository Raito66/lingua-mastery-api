package com.linguamastery.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_word_status",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_id"}))
@Data
@NoArgsConstructor
public class UserWordStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    /**
     * 熟練度等級
     * 0 = 未學習（從未練習）
     * 1 = 學習中（練習過，但連續答對未達 3 次）
     * 2 = 已熟悉（連續答對 3 次以上）
     * 3 = 已精通（SRS 間隔超過 21 天）
     */
    @Column(nullable = false)
    private int level = 0;

    @Column(name = "correct_streak", nullable = false)
    private int correctStreak = 0;
}
