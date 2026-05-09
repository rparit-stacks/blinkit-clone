package com.nainital.backend.user.dto;

import lombok.Data;

@Data
public class AddressRequest {

    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
    private boolean defaultAddress;
}
