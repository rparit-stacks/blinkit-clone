package com.nainital.backend.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAuthResponse {
    private String token;
    private String sellerId;
    private String email;
    private String fullName;
    private String storeName;
    private String storeCategory;
    private String status;
    private String storeId;
}
