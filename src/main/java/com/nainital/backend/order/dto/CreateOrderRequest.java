package com.nainital.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateOrderRequest {
    private String addressId;

    @NotBlank
    private String addressSnapshotJson;

    @NotBlank
    @Pattern(regexp = "cod|razorpay")
    private String paymentMode;

    // Client-generated UUID to prevent duplicate submissions
    @NotBlank
    private String idempotencyKey;

    // Optional coupon code (future)
    private String couponCode;
}
