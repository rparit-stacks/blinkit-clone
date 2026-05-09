package com.nainital.backend.order.service;

import com.nainital.backend.catalog.model.Product;
import com.nainital.backend.catalog.repository.ProductRepository;
import com.nainital.backend.order.dto.CartDto;
import com.nainital.backend.order.dto.CartItemRequest;
import com.nainital.backend.order.model.Cart;
import com.nainital.backend.order.model.CartItem;
import com.nainital.backend.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    @Value("${order.delivery-fee-threshold:500}")
    private int deliveryFeeThreshold;

    @Value("${order.delivery-fee:30}")
    private int deliveryFee;

    @Value("${order.tax-rate:0.05}")
    private double taxRate;

    public CartDto getCart(String userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).items(new ArrayList<>()).build());
        return toDto(cart);
    }

    public CartDto upsertItem(String userId, CartItemRequest req) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).items(new ArrayList<>()).build());

        List<CartItem> items = cart.getItems();
        items.removeIf(i -> i.getProductId().equals(req.getProductId()));

        if (req.getQuantity() > 0) {
            Product product = productRepo.findById(req.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.getProductId()));
            items.add(CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImage())
                    .storeCategory(product.getStoreCategory().name().toLowerCase())
                    .storeId(product.getStoreId())
                    .restaurantId(product.getRestaurantId())
                    .price(product.getPrice())
                    .originalPrice(product.getOriginalPrice())
                    .unit(product.getUnit())
                    .quantity(req.getQuantity())
                    .build());
        }

        cart.setItems(items);
        cartRepo.save(cart);
        return toDto(cart);
    }

    public CartDto clearCart(String userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).items(new ArrayList<>()).build());
        cart.setItems(new ArrayList<>());
        cartRepo.save(cart);
        return toDto(cart);
    }

    // Called internally after order placement
    public void clearCartInternal(String userId) {
        cartRepo.findByUserId(userId).ifPresent(cart -> {
            cart.setItems(new ArrayList<>());
            cartRepo.save(cart);
        });
    }

    // ─── Pricing ──────────────────────────────────────────────────────────────

    public int calcSubtotal(List<CartItem> items) {
        return items.stream().mapToInt(i -> i.getPrice() * i.getQuantity()).sum();
    }

    public int calcDeliveryFee(int subtotal) {
        return subtotal >= deliveryFeeThreshold ? 0 : deliveryFee;
    }

    public int calcTaxes(int subtotal) {
        return (int) Math.round(subtotal * taxRate);
    }

    public int calcTotal(int subtotal, int delivery, int taxes, int discount) {
        return subtotal + delivery + taxes - discount;
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private CartDto toDto(Cart cart) {
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : List.of();
        int subtotal = calcSubtotal(items);
        int fee = calcDeliveryFee(subtotal);
        int taxes = calcTaxes(subtotal);
        int total = calcTotal(subtotal, fee, taxes, 0);
        int count = items.stream().mapToInt(CartItem::getQuantity).sum();

        List<CartDto.CartItemDto> dtos = items.stream()
                .map(i -> CartDto.CartItemDto.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .productImage(i.getProductImage())
                        .storeCategory(i.getStoreCategory())
                        .storeId(i.getStoreId())
                        .restaurantId(i.getRestaurantId())
                        .price(i.getPrice())
                        .originalPrice(i.getOriginalPrice())
                        .unit(i.getUnit())
                        .quantity(i.getQuantity())
                        .lineTotal(i.getPrice() * i.getQuantity())
                        .build())
                .toList();

        return CartDto.builder()
                .items(dtos)
                .subtotal(subtotal)
                .deliveryFee(fee)
                .taxes(taxes)
                .total(total)
                .itemCount(count)
                .build();
    }
}
