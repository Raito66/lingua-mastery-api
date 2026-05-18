package com.linguamastery.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StreakResponse {
    private int streak;
    private int todayCount;
}
