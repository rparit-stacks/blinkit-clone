package com.nainital.backend.delivery.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.common.OtpService;
import com.nainital.backend.delivery.dto.*;
import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.service.DeliveryPartnerService;
import com.nainital.backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryAuthController {

    private final DeliveryPartnerService service;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    // ─── Public endpoints ─────────────────────────────────────────────────────

    @PostMapping("/auth/send-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
        }
        String devOtp = otpService.sendOtp(email, "Delivery Partner Portal");
        Map<String, String> resp = devOtp != null ? Map.of("otp", devOtp) : Map.of();
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to " + email, resp));
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        otpService.verifyOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.ok("Email verified", Map.of("verified", "true")));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<DeliveryAuthResponse>> register(
            @Valid @RequestBody DeliveryRegisterRequest req) {
        DeliveryPartner partner = service.register(req);
        String token = jwtUtil.generateDeliveryToken(partner.getId(), partner.getPhone());
        return ResponseEntity.ok(ApiResponse.ok("Registered successfully. Await admin approval.",
                toAuthResponse(partner, token)));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<DeliveryAuthResponse>> login(
            @Valid @RequestBody DeliveryLoginRequest req) {
        String token = service.login(req.getPhone(), req.getPassword());
        DeliveryPartner partner = service.getProfile(extractPartnerIdFromToken(token));
        return ResponseEntity.ok(ApiResponse.ok("Login successful", toAuthResponse(partner, token)));
    }

    // ─── Authenticated endpoints ──────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DeliveryPartner>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        DeliveryPartner partner = service.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(partner));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<DeliveryPartner>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateDeliveryProfileRequest req) {
        DeliveryPartner partner = service.updateProfile(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", partner));
    }

    @PostMapping("/toggle-online")
    public ResponseEntity<ApiResponse<DeliveryPartner>> toggleOnline(
            @AuthenticationPrincipal UserDetails userDetails) {
        DeliveryPartner partner = service.toggleOnline(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(
                partner.isOnline() ? "You are now online" : "You are now offline", partner));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private DeliveryAuthResponse toAuthResponse(DeliveryPartner partner, String token) {
        return DeliveryAuthResponse.builder()
                .token(token)
                .partnerId(partner.getId())
                .name(partner.getName())
                .phone(partner.getPhone())
                .status(partner.getStatus() != null ? partner.getStatus().name() : null)
                .vehicleType(partner.getVehicleType())
                .online(partner.isOnline())
                .build();
    }

    /**
     * The JWT subject IS the partnerId — extract it from the JwtUtil-generated token.
     * We parse manually here to avoid injecting JwtUtil (service.login already generates token).
     * Instead we just decode the subject from the Base64 payload.
     */
    private String extractPartnerIdFromToken(String token) {
        // token format: header.payload.signature (all Base64url)
        String payload = token.split("\\.")[1];
        // pad if needed
        int mod = payload.length() % 4;
        if (mod != 0) payload += "=".repeat(4 - mod);
        String json = new String(java.util.Base64.getUrlDecoder().decode(payload));
        // extract "sub" field
        int subIdx = json.indexOf("\"sub\"");
        if (subIdx < 0) return "";
        int colon = json.indexOf(':', subIdx);
        int q1 = json.indexOf('"', colon);
        int q2 = json.indexOf('"', q1 + 1);
        return json.substring(q1 + 1, q2);
    }
}
