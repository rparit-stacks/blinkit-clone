package com.nainital.backend.seller.dto;

import lombok.Data;

@Data
public class SellerRegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
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
}
