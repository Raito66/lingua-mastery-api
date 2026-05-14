package com.linguamastery.api.dto;

import com.linguamastery.api.model.Language;
import com.linguamastery.api.model.WordLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WordResponse {
    private Long id;
    private String word;
    private String reading;
    private String translation;
    private String example;
    private WordLevel level;
    private Language language;
    private LocalDateTime createdAt;
}
