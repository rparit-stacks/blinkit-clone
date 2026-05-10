package com.nainital.backend.delivery.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("delivery_assignments")
public class DeliveryAssignment {

    @Id
    private String id;

    /** Human-readable ID e.g. "DEL-A1B2C3" */
    private String displayId;

    @Indexed
    private String subOrderId;

    private String masterOrderId;

    @Indexed
    private String deliveryPartnerId;

    private String sellerId;
    private String storeId;
    private String customerId;

    /** Pickup address snapshot */
    private String pickupAddress;

    /** Delivery address snapshot from SubOrder.addressSnapshot */
    private String deliveryAddress;

    private String sellerStoreName;
    private String sellerPhone;

    private String customerName;
    private String customerPhone;

    /** Brief description e.g. "3 items, ₹450" */
    private String orderSummary;

    /** cod | razorpay */
    private String paymentMode;

    private boolean paid;

    /** Total order value in paise */
    private int orderTotal;

    /** Amount delivery partner earns in paise */
    private int deliveryFee;

    @Builder.Default
    private DeliveryAssignmentStatus status = DeliveryAssignmentStatus.ASSIGNED;

    private Instant assignedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant cancelledAt;

    private String cancelReason;
    private String notes;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
