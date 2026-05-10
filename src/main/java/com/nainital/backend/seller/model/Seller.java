package com.nainital.backend.seller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("sellers")
public class Seller {

    @Id
    private String id;

    // Auth
    @Indexed(unique = true)
    private String email;
    private String passwordHash;

    // Personal info
    private String fullName;
    private String phone;

    // Business info
    private String storeName;
    private String storeCategory;   // food | bazaar | electronics
    private String description;
    private String gstNumber;
    private String panNumber;
    private String businessRegNumber;

    // Address
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;

    // Bank details
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountHolderName;
    private String bankName;

    // Documents (file URLs/paths)
    private String gstCertificateUrl;
    private String panCardUrl;
    private String licenseUrl;
    private String businessProofUrl;
    private String idProofUrl;

    // Store media
    private String logoUrl;
    private String bannerUrl;
    private List<String> storeImages;

    // Linked Store ID (set after admin creates/links the store)
    private String storeId;

    // Status
    private SellerStatus status;   // PENDING | APPROVED | REJECTED

    // Rejection reason (set by admin if rejected)
    private String rejectionReason;

    // Store open/close
    private boolean storeOpen;

    // Business hours
    private String openTime;   // e.g. "09:00"
    private String closeTime;  // e.g. "22:00"

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
