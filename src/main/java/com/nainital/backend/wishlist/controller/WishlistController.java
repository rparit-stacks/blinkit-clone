package com.nainital.backend.wishlist.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.wishlist.dto.WishlistProductDto;
import com.nainital.backend.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistProductDto>>> getWishlist(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getWishlist(user.getUsername())));
    }

    // Returns all wishlisted product IDs — used by frontend to highlight hearts
    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<Set<String>>> getWishlistedIds(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getWishlistedIds(user.getUsername())));
    }

    // Toggle: adds if not present, removes if present
    @PostMapping("/{productId}/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String productId) {
        boolean added = wishlistService.toggle(user.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.ok(added ? "Added to wishlist" : "Removed from wishlist",
                Map.of("wishlisted", added, "productId", productId)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String productId) {
        wishlistService.removeItem(user.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Removed", null));
    }
}
