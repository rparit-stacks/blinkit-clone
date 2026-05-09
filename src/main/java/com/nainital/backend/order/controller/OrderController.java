package com.nainital.backend.order.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.order.dto.*;
import com.nainital.backend.order.model.Order;
import com.nainital.backend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CreateOrderRequest req) {
        try {
            CreateOrderResponse res = orderService.createOrder(user.getUsername(), req);
            return ResponseEntity.ok(ApiResponse.ok("Order created", res));
        } catch (OrderService.DuplicateOrderException e) {
            // Idempotent: return the existing order as if it was just created
            Order existing = e.getExistingOrder();
            CreateOrderResponse res = CreateOrderResponse.builder()
                    .orderId(existing.getId())
                    .status(existing.getStatus().name())
                    .razorpayOrderId(existing.getRazorpayOrderId())
                    .build();
            return ResponseEntity.ok(ApiResponse.ok("Existing order returned", res));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<OrderDto>> verifyPayment(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody PaymentVerifyRequest req) {
        OrderDto order = orderService.verifyPayment(user.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.ok("Payment verified", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getMyOrders(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(user.getUsername())));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(user.getUsername(), orderId)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.cancelOrder(user.getUsername(), orderId)));
    }
}
