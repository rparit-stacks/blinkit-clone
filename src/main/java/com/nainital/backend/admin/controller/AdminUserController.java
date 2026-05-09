package com.nainital.backend.admin.controller;

import com.nainital.backend.admin.dto.AdminCreateUserRequest;
import com.nainital.backend.admin.dto.AdminUpdateUserRequest;
import com.nainital.backend.admin.service.AdminService;
import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUser(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> create(@RequestBody AdminCreateUserRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Created", adminService.createUser(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> update(
            @PathVariable String id, @RequestBody AdminUpdateUserRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", adminService.updateUser(id, req)));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<ApiResponse<User>> block(
            @PathVariable String id, @RequestParam boolean block) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.blockUser(id, block)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}
