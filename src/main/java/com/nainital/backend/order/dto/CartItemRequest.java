package com.nainital.backend.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotBlank
    private String productId;
    @Min(0)
    private int quantity; // 0 = remove
}
