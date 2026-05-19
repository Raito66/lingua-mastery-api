package com.linguamastery.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuizQuestion {
    private Long wordId;
    private String word;
    private String reading;
    private String language;
    private List<String> options;   // 4 個選項（已打亂）
    private int correctIndex;       // 正確答案在 options 的 index
}
