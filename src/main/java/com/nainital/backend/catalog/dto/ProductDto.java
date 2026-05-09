package com.nainital.backend.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String id;
    private String storeCategory;  // "food" | "bazaar" | "electronic"
    private String storeId;
    private String categorySlug;
    private String name;
    private String description;
    private String image;
    private int price;
    private int originalPrice;
    private String unit;
    private String badge;
    private double rating;
    private boolean available;
    private String restaurantId;
}
