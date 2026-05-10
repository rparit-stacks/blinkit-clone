package com.nainital.backend.suborder.model;

import com.nainital.backend.order.model.OrderItem;
import com.nainital.backend.order.model.OrderStatus;
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

/**
 * A sub-order is the seller-specific slice of a master Order.
 * One master order creates N sub-orders, one per distinct storeId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("sub_orders")
public class SubOrder {

    @Id
    private String id;

    // Human-readable display ID e.g. "SUB-A3F9K2"
    private String displayId;

    @Indexed
    private String masterOrderId;   // links back to Order

    @Indexed
    private String sellerId;        // Seller entity id

    @Indexed
    private String storeId;         // Store entity id

    @Indexed
    private String customerId;      // userId who placed the order

    // Only the items belonging to THIS seller
    private List<OrderItem> items;

    // Pricing breakdown for this seller's slice
    private int subtotal;           // sum of lineTotals
    private int deliveryFee;        // proportional delivery fee
    private int taxes;
    private int discount;
    private int total;

    // Customer address snapshot
    private String addressSnapshot;

    // Payment info (mirrors master)
    private String paymentMode;     // cod | razorpay
    private boolean paid;

    private OrderStatus status;

    // Earnings tracking
    private int commissionRate;     // platform commission % (e.g. 10)
    private int commissionAmount;   // paise commission deducted
    private int sellerEarning;      // total - commission = seller's cut

    // Wallet settlement flag
    private boolean earningCredited;  // true once wallet credited

    // Delivery ready fields
    private String deliveryPartnerId;   // future use
    private Instant estimatedPickupAt;  // future use
    private Instant deliveredAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
