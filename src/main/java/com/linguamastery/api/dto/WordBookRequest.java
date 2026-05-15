package com.linguamastery.api.dto;

import com.linguamastery.api.model.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WordBookRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Language language;
}
