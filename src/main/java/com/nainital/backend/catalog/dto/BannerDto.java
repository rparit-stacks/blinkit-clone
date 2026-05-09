package com.nainital.backend.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerDto {
    private String id;
    private String storeCategory;
    private String title;
    private String subtitle;
    private String code;
    private String imageUrl;
}
