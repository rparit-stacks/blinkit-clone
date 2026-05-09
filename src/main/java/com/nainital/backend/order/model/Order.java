package com.nainital.backend.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("orders")
public class Order {

    @Id
    private String id;

    @Indexed
    private String userId;

    private List<OrderItem> items;

    // Pricing
    private int subtotal;
    private int deliveryFee;
    private int taxes;
    private int discount;
    private int total;

    // Address snapshot (frozen at order time)
    private String addressId;
    private String addressSnapshot; // JSON string

    // Payment
    private String paymentMode;    // cod | razorpay
    private String razorpayOrderId;
    private String razorpayPaymentId;

    private OrderStatus status;

    // Idempotency key from client to prevent duplicate orders
    private String idempotencyKey;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
