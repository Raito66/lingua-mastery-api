package com.linguamastery.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_logs")
@Data
@NoArgsConstructor
public class StudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "studied_at", nullable = false, updatable = false)
    private LocalDateTime studiedAt;

    @PrePersist
    protected void onCreate() {
        studiedAt = LocalDateTime.now();
    }
}
