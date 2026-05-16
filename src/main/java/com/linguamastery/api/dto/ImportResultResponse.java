package com.linguamastery.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ImportResultResponse {
    private int total;
    private int success;
    private int failed;
    private List<String> errors;
}
