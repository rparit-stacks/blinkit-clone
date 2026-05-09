package com.nainital.backend.coupon.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("coupons")
public class Coupon {

    @Id private String id;

    @Indexed(unique = true)
    private String code;

    private String description;

    private DiscountType discountType;  // PERCENT | FLAT
    private int discountValue;          // percent 0-100 or flat rupees
    private int minOrderValue;          // minimum cart value
    private int maxDiscount;            // cap for percent coupons (0 = no cap)

    private int usageLimit;             // 0 = unlimited
    private int usedCount;

    // Scope (null = all)
    private String storeCategory;       // food | bazaar | electronic | null
    private String storeId;

    @Builder.Default
    private boolean active = true;

    private Instant expiresAt;

    @CreatedDate  private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
