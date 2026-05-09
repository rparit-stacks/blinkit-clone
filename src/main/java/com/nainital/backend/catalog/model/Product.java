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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("products")
public class Product {

    @Id
    private String id;

    private StoreCategory storeCategory;

    /** The store/vendor/restaurant this product belongs to */
    private String storeId;

    /** Sub-category slug e.g. "biryani", "fresh", "mobiles" */
    private String categorySlug;

    private String name;
    private String description;

    /** Primary image URL */
    private String image;

    /** Additional images */
    private List<String> images;

    private int price;
    private int originalPrice;

    /** Weight / size descriptor shown on card: "1 kg", "256 GB", "350 g" */
    private String unit;

    /** Badge text: "BEST SELLER", "NEW", "FRESH", etc. */
    private String badge;

    private double rating;
    private boolean available;

    /**
     * When false, product is hidden from public catalog (moderation). Null or true = visible if {@link #available}.
     */
    private Boolean approved;

    private int sortOrder;

    /** Food only — links product to a restaurant */
    private String restaurantId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
