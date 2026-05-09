package com.nainital.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private String orderId;
    private String status;

    // Razorpay fields (null for COD)
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Integer amountPaise;
    private String currency;
}
