package com.nainital.backend.cms.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

/**
 * Represents a homepage content section (e.g. Trending, Flash Sale, Featured).
 * Each section has a type, a title, and a list of product IDs or image URLs to display.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("cms_sections")
public class CmsSection {

    @Id private String id;

    private String title;         // "Trending Now", "Flash Sale", "Staff Picks"
    private SectionType type;     // TRENDING | FLASH_SALE | FEATURED | BANNER_STRIP
    private String storeCategory; // food | bazaar | electronic | null (all)
    private String subtitle;      // Optional tagline shown below title
    private String badgeLabel;    // Optional pill badge label e.g. "HOT"
    private String badgeColor;    // Tailwind color class e.g. "bg-red-500"
    private String icon;          // emoji or icon name e.g. "🔥"

    private List<String> productIds;  // For TRENDING, FEATURED, FLASH_SALE
    private List<BannerSlide> slides; // For BANNER_STRIP type

    private boolean active;
    private int sortOrder;

    @CreatedDate  private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BannerSlide {
        private String title;
        private String subtitle;
        private String imageUrl;
        private String linkUrl;
        private String couponCode;
        private String bgGradient; // e.g. "from-violet-600 to-purple-800"
    }
}
