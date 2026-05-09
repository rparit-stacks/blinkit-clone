package com.nainital.backend.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {
    private String id;
    private String storeCategory;
    private String name;
    private String description;
    private String image;
    private String coverImage;
    private String cuisineTypes;
    private String eta;
    private Double rating;
    private String offer;
}
