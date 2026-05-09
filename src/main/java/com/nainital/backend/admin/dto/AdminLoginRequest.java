package com.nainital.backend.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequest {
    @Email @NotBlank public String email;
    @NotBlank public String password;
}
