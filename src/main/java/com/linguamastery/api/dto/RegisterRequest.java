package com.linguamastery.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
        message = "密碼至少 8 碼，須包含英文字母與數字"
    )
    private String password;

    @NotBlank(message = "請輸入顯示名稱")
    @Size(max = 50, message = "顯示名稱不能超過 50 字元")
    private String displayName;
}
