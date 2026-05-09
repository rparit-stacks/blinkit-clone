package com.nainital.backend.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Product sub-categories within a StoreCategory.
 * e.g. for FOOD: biryani, pizza, burger, chinese, dessert, thali
 * e.g. for BAZAAR: fresh, fruits, dairy, grocery, spices, cleaning
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("product_categories")
public class ProductCategory {

    @Id
    private String id;

    private StoreCategory storeCategory;

    private String slug;   // e.g. "biryani"
    private String label;  // e.g. "Biryani"
    private String icon;   // FA icon name

    private int sortOrder;
    private boolean active;
}
