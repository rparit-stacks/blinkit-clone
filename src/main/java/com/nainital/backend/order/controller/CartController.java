package com.nainital.backend.order.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.order.dto.CartDto;
import com.nainital.backend.order.dto.CartItemRequest;
import com.nainital.backend.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(user.getUsername())));
    }

    @PutMapping("/items")
    public ResponseEntity<ApiResponse<CartDto>> upsertItem(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CartItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.upsertItem(user.getUsername(), req)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartDto>> clearCart(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.clearCart(user.getUsername())));
    }
}
