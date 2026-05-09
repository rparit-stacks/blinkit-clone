package com.nainital.backend.admin.service;

import com.nainital.backend.order.model.Order;
import com.nainital.backend.order.model.OrderStatus;
import com.nainital.backend.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepo;

    public List<Order> getAllOrders() {
        return orderRepo.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }).toList();
    }

    public List<Order> getOrdersByStatus(String status) {
        try {
            OrderStatus s = OrderStatus.valueOf(status.toUpperCase());
            return orderRepo.findAll().stream()
                    .filter(o -> o.getStatus() == s)
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderRepo.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrder(String id) {
        return orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public Order updateStatus(String id, String status) {
        Order order = getOrder(id);
        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + status);
        }
        return orderRepo.save(order);
    }

    public Order cancelOrder(String id) {
        Order order = getOrder(id);
        if (order.getStatus() == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel a delivered order");
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepo.save(order);
    }
}
