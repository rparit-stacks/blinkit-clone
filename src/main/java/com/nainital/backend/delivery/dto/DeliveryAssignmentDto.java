package com.nainital.backend.delivery.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class DeliveryAssignmentDto {

    private String id;
    private String displayId;
    private String subOrderId;
    private String masterOrderId;
    private String deliveryPartnerId;
    private String sellerId;
    private String storeId;
    private String customerId;
    private String pickupAddress;
    private String deliveryAddress;
    private String sellerStoreName;
    private String sellerPhone;
    private String customerName;
    private String customerPhone;
    private String orderSummary;
    private String paymentMode;
    private boolean paid;
    private int orderTotal;
    private int deliveryFee;
    private String status;
    private Instant assignedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant cancelledAt;
    private String cancelReason;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
