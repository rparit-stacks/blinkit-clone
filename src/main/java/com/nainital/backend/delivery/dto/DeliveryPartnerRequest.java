package com.nainital.backend.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryPartnerRequest {
    @NotBlank public String name;
    @NotBlank public String phone;
    public String email;
    public String vehicleType;
    public String vehicleNumber;
}
