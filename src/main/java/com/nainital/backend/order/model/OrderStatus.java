package com.nainital.backend.order.model;

public enum OrderStatus {
    PENDING,       // created, awaiting payment
    PAID,          // payment confirmed
    PROCESSING,    // vendor is preparing
    DISPATCHED,    // out for delivery
    DELIVERED,     // completed
    CANCELLED
}
