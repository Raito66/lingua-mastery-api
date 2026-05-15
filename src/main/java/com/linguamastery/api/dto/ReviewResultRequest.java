package com.linguamastery.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewResultRequest {

    @NotNull
    private Long wordId;

    @NotNull
    private Boolean correct;
}
