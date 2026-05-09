package com.nainital.backend.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Represents a vendor/store under a platform category (Food, Bazaar, Electronic).
 * For Food: this is a restaurant.
 * For Bazaar/Electronic: this is a shop/vendor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("stores")
public class Store {

    @Id
    private String id;

    private StoreCategory storeCategory;

    private String name;
    private String description;
    private String image;
    private String coverImage;

    // Food-specific
    private String cuisineTypes; // e.g. "Biryani, Mughlai, Sweets"
    private String eta;          // e.g. "25-30 mins"
    private Double rating;
    private String offer;        // promotional text

    // Bazaar/Electronic specific
    private String address;
    private String phone;

    private boolean active;
    private int sortOrder;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
