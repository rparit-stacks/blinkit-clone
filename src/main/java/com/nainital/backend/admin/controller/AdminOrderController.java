package com.nainital.backend.admin.controller;

import com.nainital.backend.admin.service.AdminOrderService;
import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAll(
            @RequestParam(required = false) String status) {
        List<Order> orders = status != null
                ? orderService.getOrdersByStatus(status)
                : orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrdersByUser(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.updateStatus(id, status)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancel(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.cancelOrder(id)));
    }
}
