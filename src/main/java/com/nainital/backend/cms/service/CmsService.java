package com.nainital.backend.cms.service;

import com.nainital.backend.catalog.dto.ProductDto;
import com.nainital.backend.catalog.model.Product;
import com.nainital.backend.catalog.repository.ProductRepository;
import com.nainital.backend.cms.dto.CmsSectionDto;
import com.nainital.backend.cms.model.CmsSection;
import com.nainital.backend.cms.model.SectionType;
import com.nainital.backend.cms.repository.CmsSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsService {

    private final CmsSectionRepository sectionRepo;
    private final ProductRepository productRepo;

    // Public: get active sections for a store category
    public List<CmsSectionDto> getActiveSections(String storeCategory) {
        List<CmsSection> sections = storeCategory != null
                ? sectionRepo.findActiveByStoreCategory(storeCategory)
                : sectionRepo.findAllByActiveTrueOrderBySortOrderAsc();
        return sections.stream().map(this::toDto).toList();
    }

    // Admin: get all sections
    public List<CmsSection> getAllSections() {
        return sectionRepo.findAll();
    }

    public CmsSection getById(String id) {
        return sectionRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Section not found"));
    }

    public CmsSection save(CmsSection section) {
        return sectionRepo.save(section);
    }

    public CmsSection update(String id, CmsSection section) {
        section.setId(id);
        return sectionRepo.save(section);
    }

    public void delete(String id) {
        sectionRepo.deleteById(id);
    }

    public CmsSection toggle(String id) {
        CmsSection s = getById(id);
        s.setActive(!s.isActive());
        return sectionRepo.save(s);
    }

    // Also expose admin banner CRUD (delegating to Banner model via CatalogService would be circular)
    // Banners are managed separately under /api/admin/banners

    private CmsSectionDto toDto(CmsSection s) {
        List<ProductDto> products = List.of();
        if (s.getType() != SectionType.BANNER_STRIP && s.getProductIds() != null && !s.getProductIds().isEmpty()) {
            Map<String, Product> pMap = productRepo.findAllById(s.getProductIds()).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            products = s.getProductIds().stream()
                    .filter(pMap::containsKey)
                    .map(pid -> {
                        Product p = pMap.get(pid);
                        return ProductDto.builder()
                                .id(p.getId())
                                .storeCategory(p.getStoreCategory() != null ? p.getStoreCategory().name().toLowerCase() : null)
                                .storeId(p.getStoreId())
                                .categorySlug(p.getCategorySlug())
                                .name(p.getName())
                                .description(p.getDescription())
                                .image(p.getImage())
                                .price(p.getPrice())
                                .originalPrice(p.getOriginalPrice())
                                .unit(p.getUnit())
                                .badge(p.getBadge())
                                .rating(p.getRating())
                                .available(p.isAvailable())
                                .restaurantId(p.getRestaurantId())
                                .build();
                    }).toList();
        }
        return CmsSectionDto.builder()
                .id(s.getId())
                .title(s.getTitle())
                .type(s.getType() != null ? s.getType().name() : null)
                .storeCategory(s.getStoreCategory())
                .subtitle(s.getSubtitle())
                .badgeLabel(s.getBadgeLabel())
                .badgeColor(s.getBadgeColor())
                .icon(s.getIcon())
                .products(products)
                .slides(s.getSlides())
                .sortOrder(s.getSortOrder())
                .build();
    }
}
