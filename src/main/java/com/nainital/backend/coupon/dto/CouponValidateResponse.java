package com.nainital.backend.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidateResponse {
    private String code;
    private String discountType;   // PERCENT | FLAT
    private int discountValue;
    private int maxDiscount;
    private int minOrderValue;
    private int discountAmount;    // actual rupee discount for this cart total
    private String description;
}
