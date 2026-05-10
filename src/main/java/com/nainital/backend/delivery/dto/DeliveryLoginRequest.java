package com.nainital.backend.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryLoginRequest {

    @NotBlank
    private String phone;

    @NotBlank
    private String password;
}
