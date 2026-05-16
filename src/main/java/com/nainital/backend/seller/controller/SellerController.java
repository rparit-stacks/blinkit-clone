package com.nainital.backend.seller.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.common.OtpService;
import com.nainital.backend.seller.dto.*;
import com.nainital.backend.seller.model.SellerProduct;
import com.nainital.backend.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final OtpService otpService;

    // ─── Auth (public) ───────────────────────────────────────────────────────

    @PostMapping("/api/seller/auth/send-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
        }
        String devOtp = otpService.sendOtp(email, "Seller Portal");
        Map<String, String> resp = devOtp != null ? Map.of("otp", devOtp) : Map.of();
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to " + email, resp));
    }

    @PostMapping("/api/seller/auth/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        otpService.verifyOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.ok("Email verified", Map.of("verified", "true")));
    }

    @PostMapping("/api/seller/auth/register")
    public ResponseEntity<ApiResponse<SellerAuthResponse>> register(@RequestBody SellerRegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.register(req)));
    }

    @PostMapping("/api/seller/auth/login")
    public ResponseEntity<ApiResponse<SellerAuthResponse>> login(@RequestBody SellerLoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.login(req)));
    }

    // ─── Profile (seller) ────────────────────────────────────────────────────

    @GetMapping("/api/seller/profile")
    public ResponseEntity<ApiResponse<SellerProfileDto>> getProfile(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.getProfile(user.getUsername())));
    }

    @PutMapping("/api/seller/profile")
    public ResponseEntity<ApiResponse<SellerProfileDto>> updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody SellerUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.updateProfile(user.getUsername(), req)));
    }

    @PostMapping("/api/seller/profile/documents")
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam String field,
            @RequestParam String url) {
        sellerService.updateDocuments(user.getUsername(), field, url);
        return ResponseEntity.ok(ApiResponse.ok("Document updated"));
    }

    @PostMapping("/api/seller/store/toggle")
    public ResponseEntity<ApiResponse<SellerProfileDto>> toggleStore(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.toggleStore(user.getUsername())));
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @GetMapping("/api/seller/dashboard")
    public ResponseEntity<ApiResponse<SellerDashboardDto>> dashboard(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.getDashboard(user.getUsername())));
    }

    // ─── Products ────────────────────────────────────────────────────────────

    @GetMapping("/api/seller/products")
    public ResponseEntity<ApiResponse<?>> listProducts(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.getProducts(user.getUsername())));
    }

    @PostMapping("/api/seller/products")
    public ResponseEntity<ApiResponse<SellerProduct>> addProduct(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody SellerProductRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.addProduct(user.getUsername(), req)));
    }

    @PutMapping("/api/seller/products/{id}")
    public ResponseEntity<ApiResponse<SellerProduct>> updateProduct(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String id,
            @RequestBody SellerProductRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.updateProduct(user.getUsername(), id, req)));
    }

    @DeleteMapping("/api/seller/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String id) {
        sellerService.deleteProduct(user.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted"));
    }

    // ─── Admin: Seller management ────────────────────────────────────────────

    @GetMapping("/api/admin/sellers")
    public ResponseEntity<ApiResponse<?>> adminListSellers() {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.getAllSellers()));
    }

    @PostMapping("/api/admin/sellers/{id}/approve")
    public ResponseEntity<ApiResponse<?>> adminApprove(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.approveSeller(id)));
    }

    @PostMapping("/api/admin/sellers/{id}/reject")
    public ResponseEntity<ApiResponse<?>> adminReject(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(sellerService.rejectSeller(id, body.get("reason"))));
    }
}
