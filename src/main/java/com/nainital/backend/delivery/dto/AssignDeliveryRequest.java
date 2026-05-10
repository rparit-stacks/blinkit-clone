package com.nainital.backend.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AssignDeliveryRequest {

    @NotBlank
    private String subOrderId;

    @NotBlank
    private String deliveryPartnerId;

    /** Delivery fee in paise that the partner earns */
    @Min(0)
    private int deliveryFee;
}
