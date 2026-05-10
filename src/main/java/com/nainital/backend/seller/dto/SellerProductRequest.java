package com.nainital.backend.seller.dto;

import lombok.Data;

import java.util.List;

@Data
public class SellerProductRequest {
    private String name;
    private String description;
    private String categorySlug;
    private int price;
    private int originalPrice;
    private String unit;
    private String badge;
    private String sku;
    private int stockQuantity;
    private double gstPercent;
    private String image;
    private List<String> images;
    private List<String> tags;
    private boolean available;
}
