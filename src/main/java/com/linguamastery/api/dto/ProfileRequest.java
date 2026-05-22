package com.linguamastery.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {

    @NotBlank(message = "顯示名稱不能為空")
    @Size(max = 50, message = "顯示名稱不能超過 50 字元")
    private String displayName;
}
