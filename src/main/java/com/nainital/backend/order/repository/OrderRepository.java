package com.nainital.backend.order.repository;

import com.nainital.backend.order.model.Order;
import com.nainital.backend.order.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findAllByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Order> findByIdAndUserId(String id, String userId);
    Optional<Order> findByIdempotencyKeyAndUserId(String key, String userId);
    long countByStatus(OrderStatus status);
}
