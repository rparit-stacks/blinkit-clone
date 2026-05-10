package com.nainital.backend.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryRegisterRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    private String email;

    @NotBlank
    private String password;

    private String vehicleType;
    private String vehicleNumber;
}
