package com.nainital.backend.cms.dto;

import com.nainital.backend.catalog.dto.ProductDto;
import com.nainital.backend.cms.model.CmsSection;
import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CmsSectionDto {
    private String id;
    private String title;
    private String type;
    private String storeCategory;
    private String subtitle;
    private String badgeLabel;
    private String badgeColor;
    private String icon;
    private List<ProductDto> products;          // resolved for TRENDING/FEATURED/FLASH_SALE
    private List<CmsSection.BannerSlide> slides; // for BANNER_STRIP
    private int sortOrder;
}
