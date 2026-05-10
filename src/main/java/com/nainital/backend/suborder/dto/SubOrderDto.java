package com.nainital.backend.suborder.dto;

import com.nainital.backend.order.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SubOrderDto {
    private String id;
    private String displayId;
    private String masterOrderId;
    private String sellerId;
    private String storeId;
    private String customerId;
    private List<OrderItem> items;
    private int subtotal;
    private int deliveryFee;
    private int taxes;
    private int discount;
    private int total;
    private String addressSnapshot;
    private String paymentMode;
    private boolean paid;
    private String status;
    private int commissionRate;
    private int commissionAmount;
    private int sellerEarning;
    private boolean earningCredited;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deliveredAt;
}
