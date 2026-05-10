package com.nainital.backend.seller.dto;

import lombok.Data;

import java.util.List;

@Data
public class SellerUpdateRequest {
    private String fullName;
    private String phone;
    private String description;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String openTime;
    private String closeTime;
    private String logoUrl;
    private String bannerUrl;
    private List<String> storeImages;
    private Boolean storeOpen;
}
