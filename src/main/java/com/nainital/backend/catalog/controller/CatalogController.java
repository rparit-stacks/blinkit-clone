package com.nainital.backend.catalog.controller;

import com.nainital.backend.catalog.dto.*;
import com.nainital.backend.catalog.model.StoreCategory;
import com.nainital.backend.catalog.service.CatalogService;
import com.nainital.backend.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    // ─── Parse helper ─────────────────────────────────────────────────────────

    private StoreCategory parseCategory(String cat) {
        return switch (cat.toLowerCase()) {
            case "food" -> StoreCategory.FOOD;
            case "bazaar" -> StoreCategory.BAZAAR;
            case "electronic", "electronics" -> StoreCategory.ELECTRONIC;
            default -> throw new IllegalArgumentException("Unknown category: " + cat);
        };
    }

    // ─── Categories ───────────────────────────────────────────────────────────

    @GetMapping("/{category}/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories(
            @PathVariable String category) {
        List<CategoryDto> data = catalogService.getCategories(parseCategory(category));
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ─── Stores / Restaurants ─────────────────────────────────────────────────

    @GetMapping("/{category}/stores")
    public ResponseEntity<ApiResponse<List<StoreDto>>> getStores(
            @PathVariable String category) {
        List<StoreDto> data = catalogService.getStores(parseCategory(category));
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/stores/{id}")
    public ResponseEntity<ApiResponse<StoreDto>> getStore(@PathVariable String id) {
        return catalogService.getStore(id)
                .map(s -> ResponseEntity.ok(ApiResponse.ok(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Products ─────────────────────────────────────────────────────────────

    @GetMapping("/{category}/products")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProducts(
            @PathVariable String category,
            @RequestParam(required = false) String cat) {
        List<ProductDto> data = catalogService.getProducts(parseCategory(category), cat);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable String id) {
        return catalogService.getProduct(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stores/{storeId}/products")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByStore(
            @PathVariable String storeId) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getProductsByStore(storeId)));
    }

    @GetMapping("/restaurants/{restaurantId}/products")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByRestaurant(
            @PathVariable String restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getProductsByRestaurant(restaurantId)));
    }

    @GetMapping("/{category}/products/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(
            @PathVariable String category,
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(
                catalogService.searchProducts(parseCategory(category), q)));
    }

    // ─── Banners ──────────────────────────────────────────────────────────────

    @GetMapping("/{category}/banners")
    public ResponseEntity<ApiResponse<List<BannerDto>>> getBanners(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.ok(
                catalogService.getBanners(parseCategory(category))));
    }

    // ─── Seed (dev only) ──────────────────────────────────────────────────────

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seed() {
        catalogService.seedAll();
        return ResponseEntity.ok(ApiResponse.ok("Seed complete"));
    }
}
