package com.linguamastery.api.dto;

import com.linguamastery.api.model.Language;
import com.linguamastery.api.model.WordLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WordRequest {

    @NotBlank
    private String word;

    private String reading;

    @NotBlank
    private String translation;

    private String example;

    private WordLevel level;

    @NotNull
    private Language language;
}
