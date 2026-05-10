package com.nainital.backend.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileDto {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String storeName;
    private String storeCategory;
    private String description;
    private String gstNumber;
    private String panNumber;
    private String businessRegNumber;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountHolderName;
    private String bankName;
    private String gstCertificateUrl;
    private String panCardUrl;
    private String licenseUrl;
    private String businessProofUrl;
    private String idProofUrl;
    private String logoUrl;
    private String bannerUrl;
    private List<String> storeImages;
    private String storeId;
    private String status;
    private String rejectionReason;
    private boolean storeOpen;
    private String openTime;
    private String closeTime;
    private Instant createdAt;
}
