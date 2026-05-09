package com.nainital.backend.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private String productName;
    private String productImage;
    private String storeCategory;  // food | bazaar | electronic
    private String storeId;
    private String restaurantId;   // food only
    private int price;
    private int originalPrice;
    private String unit;
    private int quantity;
}
