package com.nainital.backend.admin.controller;

import com.nainital.backend.admin.dto.*;
import com.nainital.backend.admin.model.Admin;
import com.nainital.backend.admin.service.AdminService;
import com.nainital.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", adminService.login(req)));
    }

    /** SUPER_ADMIN: create another admin account */
    @PostMapping("/admins")
    public ResponseEntity<ApiResponse<Admin>> createAdmin(
            @Valid @RequestBody CreateAdminRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Admin created", adminService.createAdmin(req)));
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<Admin>>> getAdmins() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllAdmins()));
    }

    @PatchMapping("/admins/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleAdmin(
            @PathVariable String id,
            @RequestParam boolean active) {
        adminService.toggleAdmin(id, active);
        return ResponseEntity.ok(ApiResponse.ok("Updated", null));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable String id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
