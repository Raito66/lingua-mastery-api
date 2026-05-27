package com.linguamastery.api.dto;

import lombok.Data;

@Data
public class BookStatsResponse {
    private long totalWords;
    private long totalStudied;
    private long totalCorrect;
    private double accuracy;
    private long notLearned;
    private long learning;
    private long familiar;
    private long mastered;
}
