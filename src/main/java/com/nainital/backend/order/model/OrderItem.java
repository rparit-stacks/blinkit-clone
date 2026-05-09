package com.nainital.backend.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
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
    private int lineTotal;  // price * quantity
}
