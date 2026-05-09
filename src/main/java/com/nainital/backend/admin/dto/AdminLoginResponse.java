package com.nainital.backend.admin.dto;

import com.nainital.backend.admin.model.AdminRole;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AdminLoginResponse {
    private String token;
    private String adminId;
    private String email;
    private String name;
    private AdminRole role;
}
