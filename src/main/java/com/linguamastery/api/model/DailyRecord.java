package com.linguamastery.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
@Data
@NoArgsConstructor
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "words_studied", nullable = false)
    private int wordsStudied = 0;
}
