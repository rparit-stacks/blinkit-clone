package com.nainital.backend.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAuthResponse {

    private String token;
    private String partnerId;
    private String name;
    private String phone;
    private String status;
    private String vehicleType;
    private boolean online;
}
