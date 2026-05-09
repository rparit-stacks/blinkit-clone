package com.nainital.backend.admin.dto;

import com.nainital.backend.admin.model.AdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAdminRequest {
    @Email @NotBlank public String email;
    @NotBlank public String password;
    @NotBlank public String name;
    public AdminRole role;
}
