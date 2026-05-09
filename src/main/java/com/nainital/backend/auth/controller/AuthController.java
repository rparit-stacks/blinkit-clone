package com.nainital.backend.auth.controller;

import com.nainital.backend.auth.dto.*;
import com.nainital.backend.auth.service.AuthService;
import com.nainital.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────────
    // Email OTP: Send
    // ─────────────────────────────────────────────

    @PostMapping("/email/send-otp")
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        SendOtpResponse response = authService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent successfully", response));
    }

    // ─────────────────────────────────────────────
    // Email OTP: Verify
    // ─────────────────────────────────────────────

    @PostMapping("/email/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        AuthResponse response = authService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully", response));
    }

    // ─────────────────────────────────────────────
    // Google OAuth
    // ─────────────────────────────────────────────

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request) {

        AuthResponse response = authService.googleAuth(request.getIdToken());
        return ResponseEntity.ok(ApiResponse.ok("Google authentication successful", response));
    }

    // ─────────────────────────────────────────────
    // Token Refresh
    // ─────────────────────────────────────────────

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    // ─────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails != null) {
            authService.logout(userDetails.getUsername());
        }
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }
}
