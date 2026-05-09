package com.nainital.backend.coupon.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.coupon.dto.CouponRequest;
import com.nainital.backend.coupon.model.Coupon;
import com.nainital.backend.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(couponService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Coupon>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> create(@Valid @RequestBody CouponRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Created", couponService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Coupon>> update(
            @PathVariable String id, @Valid @RequestBody CouponRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", couponService.update(id, req)));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Coupon>> toggle(
            @PathVariable String id, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.toggleActive(id, active)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        couponService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    // Public: validate coupon at checkout
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Coupon>> validate(
            @RequestParam String code, @RequestParam int orderTotal) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.validate(code, orderTotal)));
    }
}
