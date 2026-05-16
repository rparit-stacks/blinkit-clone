package com.nainital.backend.user.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.user.dto.AddressRequest;
import com.nainital.backend.user.dto.UpdateProfileRequest;
import com.nainital.backend.user.model.Address;
import com.nainital.backend.user.model.User;
import com.nainital.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────
    // Profile
    // ─────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved successfully", profile));
    }

    @GetMapping("/profile/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfileMe(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {

        User user = userService.updateProfile(
                userDetails.getUsername(),
                request.getName(),
                request.getPhone(),
                request.getProfileImage(),
                request.getGender(),
                request.getDateOfBirth()
        );
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", user));
    }

    // ─────────────────────────────────────────────
    // Addresses
    // ─────────────────────────────────────────────

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<Address>>> getAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Address> addresses = userService.getAddresses(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Addresses retrieved successfully", addresses));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<Address>> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressRequest request) {

        Address address = userService.addAddress(
                userDetails.getUsername(),
                request.getLabel(),
                request.getLine1(),
                request.getLine2(),
                request.getCity(),
                request.getState(),
                request.getPincode(),
                request.isDefaultAddress()
        );
        return ResponseEntity.ok(ApiResponse.ok("Address added successfully", address));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<Address>> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody AddressRequest request) {

        Address address = userService.updateAddress(
                userDetails.getUsername(),
                id,
                request.getLabel(),
                request.getLine1(),
                request.getLine2(),
                request.getCity(),
                request.getState(),
                request.getPincode(),
                request.isDefaultAddress()
        );
        return ResponseEntity.ok(ApiResponse.ok("Address updated successfully", address));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        userService.deleteAddress(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted successfully", null));
    }

    @PutMapping("/addresses/{id}/default")
    public ResponseEntity<ApiResponse<Address>> setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        Address address = userService.setDefaultAddress(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Default address updated", address));
    }

    // ─────────────────────────────────────────────
    // Push Notifications (FCM)
    // ─────────────────────────────────────────────

    @PostMapping("/push-token")
    public ResponseEntity<ApiResponse<String>> registerPushToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("token is required"));
        }
        userService.registerFcmToken(userDetails.getUsername(), token);
        return ResponseEntity.ok(ApiResponse.ok("Push token registered", "ok"));
    }

    @DeleteMapping("/push-token")
    public ResponseEntity<ApiResponse<String>> removePushToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null && !token.isBlank()) {
            userService.removeFcmToken(userDetails.getUsername(), token);
        }
        return ResponseEntity.ok(ApiResponse.ok("Push token removed", "ok"));
    }
}
