package com.nainital.backend.notification.service;

import com.nainital.backend.delivery.model.DeliveryAssignment;
import com.nainital.backend.delivery.model.DeliveryAssignmentStatus;
import com.nainital.backend.notification.client.NotificationClient;
import com.nainital.backend.notification.dto.NotificationRecipient;
import com.nainital.backend.order.model.Order;
import com.nainital.backend.order.model.OrderStatus;
import com.nainital.backend.suborder.model.SubOrder;
import com.nainital.backend.wallet.model.WalletOwnerType;
import com.nainital.backend.wallet.model.WithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationClient client;

    @Async("notificationExecutor")
    public void orderPlaced(Order order) {
        client.submit(
                "SYSTEM", order.getUserId(), "ORDER",
                List.of(new NotificationRecipient(order.getUserId(), "CUSTOMER")),
                null,
                "ORDER_PLACED",
                "Order confirmed",
                "Your order has been placed and is being processed.",
                "HIGH",
                "ORDER", order.getId(),
                "order-placed:" + order.getId()
        );
    }

    @Async("notificationExecutor")
    public void orderCancelled(String customerId, String orderId) {
        client.submit(
                "SYSTEM", customerId, "ORDER",
                List.of(new NotificationRecipient(customerId, "CUSTOMER")),
                null,
                "ORDER_CANCELLED",
                "Order cancelled",
                "Your order was cancelled.",
                "HIGH",
                "ORDER", orderId,
                "order-cancelled:" + orderId
        );
    }

    @Async("notificationExecutor")
    public void newSubOrderForSeller(SubOrder sub) {
        if (sub.getSellerId() == null) return;
        client.submit(
                "SYSTEM", sub.getMasterOrderId(), "SUB_ORDER",
                List.of(new NotificationRecipient(sub.getSellerId(), "SELLER")),
                null,
                "NEW_SUB_ORDER",
                "New order received",
                "You have a new order " + sub.getDisplayId() + " to fulfill.",
                "HIGH",
                "SUB_ORDER", sub.getId(),
                "suborder-new:" + sub.getId()
        );
    }

    @Async("notificationExecutor")
    public void subOrderStatusForCustomer(SubOrder sub, OrderStatus status) {
        // Fulfillment updates for customer are sent by delivery flow (DISPATCHED/DELIVERED).
        if (status == OrderStatus.DISPATCHED || status == OrderStatus.DELIVERED) {
            return;
        }
        if (sub.getCustomerId() == null || sub.getCustomerId().isBlank()) {
            return;
        }
        String title = switch (status) {
            case PROCESSING -> "Order is being prepared";
            case CANCELLED -> "Order cancelled";
            default -> null;
        };
        String body = switch (status) {
            case PROCESSING -> "The store is preparing your items for order " + sub.getDisplayId() + ".";
            case CANCELLED -> "Part of your order was cancelled.";
            default -> null;
        };
        if (title == null) return;
        client.submit(
                "SYSTEM", sub.getSellerId(), "SUB_ORDER",
                List.of(new NotificationRecipient(sub.getCustomerId(), "CUSTOMER")),
                null,
                "SUB_ORDER_STATUS",
                title,
                body,
                status == OrderStatus.DELIVERED ? "HIGH" : "NORMAL",
                "SUB_ORDER", sub.getId(),
                "suborder-status:" + sub.getId() + ":" + status.name()
        );
    }

    @Async("notificationExecutor")
    public void deliveryAssigned(DeliveryAssignment assignment) {
        client.submit(
                "SYSTEM", assignment.getSubOrderId(), "DELIVERY",
                List.of(new NotificationRecipient(assignment.getDeliveryPartnerId(), "DELIVERY")),
                null,
                "DELIVERY_ASSIGNED",
                "New delivery assignment",
                "You have been assigned delivery " + assignment.getDisplayId() + ".",
                "HIGH",
                "DELIVERY_ASSIGNMENT", assignment.getId(),
                "delivery-assigned-partner:" + assignment.getId()
        );
        if (assignment.getCustomerId() != null && !assignment.getCustomerId().isBlank()) {
            client.submit(
                    "SYSTEM", assignment.getSubOrderId(), "DELIVERY",
                    List.of(new NotificationRecipient(assignment.getCustomerId(), "CUSTOMER")),
                    null,
                    "DELIVERY_ASSIGNED",
                    "Delivery partner assigned",
                    "A delivery partner has been assigned to your order.",
                    "NORMAL",
                    "DELIVERY_ASSIGNMENT", assignment.getId(),
                    "delivery-assigned-customer:" + assignment.getId()
            );
        }
    }

    @Async("notificationExecutor")
    public void deliveryStatusForCustomer(DeliveryAssignment assignment, DeliveryAssignmentStatus status) {
        if (assignment.getCustomerId() == null) return;
        String title;
        String body;
        switch (status) {
            case PICKED_UP -> {
                title = "Order picked up";
                body = "Your order has been picked up and is on the way.";
            }
            case ON_THE_WAY -> {
                title = "On the way";
                body = "Your delivery partner is heading to you.";
            }
            case DELIVERED -> {
                title = "Delivered";
                body = "Your order has been delivered.";
            }
            default -> {
                return;
            }
        }
        client.submit(
                "SYSTEM", assignment.getDeliveryPartnerId(), "DELIVERY",
                List.of(new NotificationRecipient(assignment.getCustomerId(), "CUSTOMER")),
                null,
                "DELIVERY_STATUS",
                title,
                body,
                "HIGH",
                "DELIVERY_ASSIGNMENT", assignment.getId(),
                "delivery-status:" + assignment.getId() + ":" + status.name()
        );
    }

    @Async("notificationExecutor")
    public void refundCredited(String customerId, String orderId, int amountRupees) {
        client.submit(
                "SYSTEM", customerId, "WALLET",
                List.of(new NotificationRecipient(customerId, "CUSTOMER")),
                null,
                "REFUND",
                "Refund credited",
                "₹" + amountRupees + " has been credited to your wallet for order " + orderId + ".",
                "HIGH",
                "ORDER", orderId,
                "refund:" + orderId
        );
    }

    @Async("notificationExecutor")
    public void withdrawalProcessed(WithdrawalRequest wr, boolean approved) {
        String role = switch (wr.getOwnerType()) {
            case SELLER -> "SELLER";
            case DELIVERY_PARTNER -> "DELIVERY";
            case CUSTOMER -> "CUSTOMER";
            default -> null;
        };
        if (role == null) return;
        String title = approved ? "Withdrawal approved" : "Withdrawal rejected";
        String body = approved
                ? "Your withdrawal request has been processed."
                : "Your withdrawal request was rejected. Amount returned to wallet.";
        client.submit(
                "SYSTEM", wr.getOwnerId(), "WALLET",
                List.of(new NotificationRecipient(wr.getOwnerId(), role)),
                null,
                approved ? "WITHDRAWAL_APPROVED" : "WITHDRAWAL_REJECTED",
                title,
                body,
                "HIGH",
                "WITHDRAWAL", wr.getId(),
                "withdrawal:" + wr.getId() + ":" + (approved ? "approved" : "rejected")
        );
    }

    @Async("notificationExecutor")
    public void sellerApproved(String sellerId) {
        client.submit(
                "SYSTEM", sellerId, "SELLER",
                List.of(new NotificationRecipient(sellerId, "SELLER")),
                null,
                "SELLER_APPROVED",
                "Store approved",
                "Your seller account has been approved. You can now start selling.",
                "HIGH",
                "SELLER", sellerId,
                "seller-approved:" + sellerId
        );
    }

    @Async("notificationExecutor")
    public void sellerRejected(String sellerId, String reason) {
        client.submit(
                "SYSTEM", sellerId, "SELLER",
                List.of(new NotificationRecipient(sellerId, "SELLER")),
                null,
                "SELLER_REJECTED",
                "Application rejected",
                reason != null && !reason.isBlank() ? reason : "Your seller application was not approved.",
                "HIGH",
                "SELLER", sellerId,
                "seller-rejected:" + sellerId
        );
    }

    @Async("notificationExecutor")
    public void sellerRegistered(String sellerId, String storeName) {
        client.submit(
                "SYSTEM", sellerId, "SELLER",
                null,
                List.of("ADMIN"),
                "SELLER_REGISTERED",
                "New seller registration",
                (storeName != null ? storeName : "A new seller") + " has registered and is pending review.",
                "HIGH",
                "SELLER", sellerId,
                "seller-registered:" + sellerId
        );
    }

    @Async("notificationExecutor")
    public void deliveryPartnerRegistered(String partnerId, String partnerName) {
        client.submit(
                "SYSTEM", partnerId, "DELIVERY",
                null,
                List.of("ADMIN"),
                "DELIVERY_REGISTERED",
                "New delivery partner registration",
                (partnerName != null ? partnerName : "A new delivery partner") + " has registered and is pending KYC review.",
                "HIGH",
                "DELIVERY_PARTNER", partnerId,
                "delivery-registered:" + partnerId
        );
    }

    @Async("notificationExecutor")
    public void deliveryPartnerApproved(String partnerId) {
        client.submit(
                "SYSTEM", partnerId, "DELIVERY",
                List.of(new NotificationRecipient(partnerId, "DELIVERY")),
                null,
                "DELIVERY_APPROVED",
                "Account approved",
                "Your delivery partner account has been approved. You can now go online and receive deliveries.",
                "HIGH",
                "DELIVERY_PARTNER", partnerId,
                "delivery-approved:" + partnerId
        );
    }

    @Async("notificationExecutor")
    public void deliveryPartnerBlocked(String partnerId, boolean blocked) {
        if (!blocked) return;
        client.submit(
                "SYSTEM", partnerId, "DELIVERY",
                List.of(new NotificationRecipient(partnerId, "DELIVERY")),
                null,
                "DELIVERY_BLOCKED",
                "Account blocked",
                "Your delivery partner account has been blocked. Contact support for assistance.",
                "HIGH",
                "DELIVERY_PARTNER", partnerId,
                "delivery-blocked:" + partnerId
        );
    }
}
