package com.nainital.backend.delivery.dto;

import lombok.Data;

@Data
public class UpdateDeliveryProfileRequest {

    private String name;
    private String email;
    private String profileImage;
    private String vehicleType;
    private String vehicleNumber;
    private String idProofUrl;
    private String vehicleImageUrl;
    private String licenseUrl;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountHolderName;
    private String bankName;
    private String upiId;
    private String currentLatitude;
    private String currentLongitude;
}
