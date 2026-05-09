package com.nainital.backend.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("banners")
public class Banner {

    @Id
    private String id;

    private StoreCategory storeCategory;

    private String title;
    private String subtitle;
    private String code;
    private String imageUrl;

    private boolean active;
    private int sortOrder;
}
