package com.nainital.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private List<CartItemDto> items;
    private int subtotal;
    private int deliveryFee;
    private int taxes;
    private int total;
    private int itemCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private String productId;
        private String productName;
        private String productImage;
        private String storeCategory;
        private String storeId;
        private String restaurantId;
        private int price;
        private int originalPrice;
        private String unit;
        private int quantity;
        private int lineTotal;
    }
}
