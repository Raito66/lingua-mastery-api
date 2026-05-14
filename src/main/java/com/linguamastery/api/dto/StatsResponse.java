package com.linguamastery.api.dto;

import lombok.Data;

@Data
public class StatsResponse {
    private long totalStudied;
    private long totalCorrect;
    private double accuracy;
}
