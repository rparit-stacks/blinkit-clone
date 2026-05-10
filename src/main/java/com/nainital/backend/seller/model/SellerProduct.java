package com.nainital.backend.seller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Seller-managed product — extends the shared catalog Product
 * with seller-specific fields (stock, SKU, GST %).
 * The linked Product (productId) is the catalog-visible record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("seller_products")
public class SellerProduct {

    @Id
    private String id;

    @Indexed
    private String sellerId;

    // Linked catalog Product id
    private String productId;

    // Store id this belongs to
    private String storeId;

    private String name;
    private String description;
    private String categorySlug;
    private String storeCategory;

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

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
