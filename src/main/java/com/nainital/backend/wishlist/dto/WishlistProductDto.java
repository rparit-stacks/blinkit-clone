package com.nainital.backend.wishlist.dto;

import lombok.*;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WishlistProductDto {
    private String wishlistItemId;
    private String productId;
    private String name;
    private String image;
    private int price;
    private int originalPrice;
    private String unit;
    private String storeCategory;
    private String badge;
    private double rating;
    private boolean available;
    private String restaurantId;
    private Instant addedAt;
}
