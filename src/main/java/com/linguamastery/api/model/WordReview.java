package com.linguamastery.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "word_reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_id"}))
@Data
@NoArgsConstructor
public class WordReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    /** SM-2: 間隔天數 */
    @Column(name = "review_interval", nullable = false)
    private int interval = 1;

    /** SM-2: 難易係數（預設 2.5，最低 1.3） */
    @Column(name = "ease_factor", nullable = false)
    private double easeFactor = 2.5;

    /** SM-2: 連續答對次數 */
    @Column(nullable = false)
    private int repetitions = 0;

    /** 下次應複習日期 */
    @Column(name = "next_review_at", nullable = false)
    private LocalDate nextReviewAt;

    /** 最後一次複習時間 */
    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;
}
