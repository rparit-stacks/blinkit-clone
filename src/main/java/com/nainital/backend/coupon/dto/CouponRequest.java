package com.nainital.backend.coupon.dto;

import com.nainital.backend.coupon.model.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;

@Data
public class CouponRequest {
    @NotBlank public String code;
    public String description;
    @NotNull public DiscountType discountType;
    public int discountValue;
    public int minOrderValue;
    public int maxDiscount;
    public int usageLimit;
    public String storeCategory;
    public String storeId;
    public boolean active;
    public Instant expiresAt;
}
