package com.nainital.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private String id;
    private String userId;
    private List<OrderItemDto> items;
    private int subtotal;
    private int deliveryFee;
    private int taxes;
    private int discount;
    private int total;
    private String addressSnapshot;
    private String paymentMode;
    private String razorpayOrderId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private String productName;
        private String productImage;
        private String storeCategory;
        private String storeId;
        private int price;
        private int quantity;
        private int lineTotal;
        private String unit;
    }
}
