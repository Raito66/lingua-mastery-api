package com.linguamastery.api.dto;

import com.linguamastery.api.model.Language;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WordBookResponse {
    private Long id;
    private String name;
    private Language language;
    private long wordCount;
    private LocalDateTime createdAt;
}
