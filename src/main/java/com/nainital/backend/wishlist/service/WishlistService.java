package com.nainital.backend.wishlist.service;

import com.nainital.backend.catalog.model.Product;
import com.nainital.backend.catalog.repository.ProductRepository;
import com.nainital.backend.wishlist.dto.WishlistProductDto;
import com.nainital.backend.wishlist.model.WishlistItem;
import com.nainital.backend.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepo;
    private final ProductRepository productRepo;

    public List<WishlistProductDto> getWishlist(String userId) {
        List<WishlistItem> items = wishlistRepo.findAllByUserIdOrderByCreatedAtDesc(userId);
        if (items.isEmpty()) return List.of();

        Set<String> productIds = items.stream().map(WishlistItem::getProductId).collect(Collectors.toSet());
        Map<String, Product> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return items.stream()
                .filter(item -> productMap.containsKey(item.getProductId()))
                .map(item -> {
                    Product p = productMap.get(item.getProductId());
                    return WishlistProductDto.builder()
                            .wishlistItemId(item.getId())
                            .productId(p.getId())
                            .name(p.getName())
                            .image(p.getImage())
                            .price(p.getPrice())
                            .originalPrice(p.getOriginalPrice())
                            .unit(p.getUnit())
                            .storeCategory(p.getStoreCategory() != null ? p.getStoreCategory().name().toLowerCase() : null)
                            .badge(p.getBadge())
                            .rating(p.getRating())
                            .available(p.isAvailable())
                            .restaurantId(p.getRestaurantId())
                            .addedAt(item.getCreatedAt())
                            .build();
                })
                .toList();
    }

    // Returns true if added, false if already present (idempotent)
    public boolean toggle(String userId, String productId) {
        if (wishlistRepo.existsByUserIdAndProductId(userId, productId)) {
            wishlistRepo.deleteByUserIdAndProductId(userId, productId);
            return false; // removed
        }
        if (!productRepo.existsById(productId))
            throw new IllegalArgumentException("Product not found");
        wishlistRepo.save(WishlistItem.builder().userId(userId).productId(productId).build());
        return true; // added
    }

    public boolean isWishlisted(String userId, String productId) {
        return wishlistRepo.existsByUserIdAndProductId(userId, productId);
    }

    public Set<String> getWishlistedIds(String userId) {
        return wishlistRepo.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(WishlistItem::getProductId).collect(Collectors.toSet());
    }

    @Transactional
    public void removeItem(String userId, String productId) {
        wishlistRepo.deleteByUserIdAndProductId(userId, productId);
    }
}
