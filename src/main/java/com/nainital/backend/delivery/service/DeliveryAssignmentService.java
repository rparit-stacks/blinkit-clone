package com.nainital.backend.delivery.service;

import com.nainital.backend.delivery.model.DeliveryAssignment;
import com.nainital.backend.delivery.model.DeliveryAssignmentStatus;
import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.repository.DeliveryAssignmentRepository;
import com.nainital.backend.delivery.repository.DeliveryPartnerRepository;
import com.nainital.backend.order.model.OrderStatus;
import com.nainital.backend.suborder.model.SubOrder;
import com.nainital.backend.suborder.repository.SubOrderRepository;
import com.nainital.backend.wallet.model.TransactionType;
import com.nainital.backend.wallet.model.WalletOwnerType;
import com.nainital.backend.notification.service.NotificationPublisher;
import com.nainital.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentService {

    private final DeliveryAssignmentRepository assignmentRepo;
    private final SubOrderRepository subOrderRepo;
    private final DeliveryPartnerRepository partnerRepo;

    @Lazy
    private final WalletService walletService;
    private final NotificationPublisher notificationPublisher;

    // ─── Utility ─────────────────────────────────────────────────────────────

    private String generateDisplayId() {
        return "DEL-" + Long.toString(System.currentTimeMillis() % 1_000_000_000L, 36).toUpperCase();
    }

    private DeliveryPartner getPartnerOrThrow(String partnerId) {
        return partnerRepo.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery partner not found: " + partnerId));
    }

    private SubOrder getSubOrderOrThrow(String subOrderId) {
        return subOrderRepo.findById(subOrderId)
                .orElseThrow(() -> new IllegalArgumentException("SubOrder not found: " + subOrderId));
    }

    // ─── Admin assigns a sub-order to a delivery partner ─────────────────────

    public DeliveryAssignment assign(String subOrderId, String deliveryPartnerId, int deliveryFeePaise) {
        SubOrder subOrder = getSubOrderOrThrow(subOrderId);
        DeliveryPartner partner = getPartnerOrThrow(deliveryPartnerId);

        // Build order summary
        int itemCount = (subOrder.getItems() == null) ? 0 : subOrder.getItems().size();
        long rupees = subOrder.getTotal();
        String orderSummary = itemCount + " item" + (itemCount == 1 ? "" : "s") + ", ₹" + (rupees / 100);

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .displayId(generateDisplayId())
                .subOrderId(subOrderId)
                .masterOrderId(subOrder.getMasterOrderId())
                .deliveryPartnerId(deliveryPartnerId)
                .sellerId(subOrder.getSellerId())
                .storeId(subOrder.getStoreId())
                .customerId(subOrder.getCustomerId())
                .pickupAddress("Seller store - " + subOrder.getStoreId())
                .deliveryAddress(subOrder.getAddressSnapshot())
                .sellerStoreName(subOrder.getStoreId())
                .sellerPhone("")
                .customerName("")
                .customerPhone("")
                .orderSummary(orderSummary)
                .paymentMode(subOrder.getPaymentMode())
                .paid(subOrder.isPaid())
                .orderTotal(subOrder.getTotal())
                .deliveryFee(deliveryFeePaise)
                .status(DeliveryAssignmentStatus.ASSIGNED)
                .assignedAt(Instant.now())
                .build();

        DeliveryAssignment saved = assignmentRepo.save(assignment);

        // Set deliveryPartnerId on SubOrder
        subOrder.setDeliveryPartnerId(deliveryPartnerId);
        subOrderRepo.save(subOrder);

        notificationPublisher.deliveryAssigned(saved);
        return saved;
    }

    // ─── Delivery partner updates assignment status ───────────────────────────

    public DeliveryAssignment updateStatus(String assignmentId, String partnerId,
                                           DeliveryAssignmentStatus newStatus) {
        DeliveryAssignment assignment = assignmentRepo.findByIdAndDeliveryPartnerId(assignmentId, partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found or not yours"));

        validateStatusTransition(assignment.getStatus(), newStatus);

        assignment.setStatus(newStatus);

        switch (newStatus) {
            case ACCEPTED -> {}
            case PICKED_UP -> {
                assignment.setPickedUpAt(Instant.now());
                updateSubOrderStatus(assignment.getSubOrderId(), OrderStatus.DISPATCHED);
            }
            case ON_THE_WAY -> {}
            case DELIVERED -> {
                assignment.setDeliveredAt(Instant.now());
                updateSubOrderStatus(assignment.getSubOrderId(), OrderStatus.DELIVERED);
                creditDeliveryEarning(assignment);
                // Increment total deliveries for partner
                partnerRepo.findById(partnerId).ifPresent(p -> {
                    p.setTotalDeliveries(p.getTotalDeliveries() + 1);
                    partnerRepo.save(p);
                });
            }
            case REJECTED -> {
                assignment.setCancelledAt(Instant.now());
            }
            case CANCELLED -> {
                assignment.setCancelledAt(Instant.now());
            }
            default -> {}
        }

        assignment = assignmentRepo.save(assignment);
        notificationPublisher.deliveryStatusForCustomer(assignment, newStatus);
        return assignment;
    }

    // ─── Admin override status ────────────────────────────────────────────────

    public DeliveryAssignment adminUpdateStatus(String assignmentId, DeliveryAssignmentStatus newStatus) {
        DeliveryAssignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        assignment.setStatus(newStatus);

        switch (newStatus) {
            case PICKED_UP -> {
                if (assignment.getPickedUpAt() == null) assignment.setPickedUpAt(Instant.now());
                updateSubOrderStatus(assignment.getSubOrderId(), OrderStatus.DISPATCHED);
            }
            case DELIVERED -> {
                if (assignment.getDeliveredAt() == null) assignment.setDeliveredAt(Instant.now());
                updateSubOrderStatus(assignment.getSubOrderId(), OrderStatus.DELIVERED);
                creditDeliveryEarning(assignment);
            }
            case CANCELLED -> {
                if (assignment.getCancelledAt() == null) assignment.setCancelledAt(Instant.now());
            }
            default -> {}
        }

        assignment = assignmentRepo.save(assignment);
        notificationPublisher.deliveryStatusForCustomer(assignment, newStatus);
        return assignment;
    }

    // ─── Wallet credit on delivery ────────────────────────────────────────────

    private void creditDeliveryEarning(DeliveryAssignment assignment) {
        if (assignment.getDeliveryFee() <= 0) return;
        try {
            walletService.credit(
                    assignment.getDeliveryPartnerId(),
                    WalletOwnerType.DELIVERY_PARTNER,
                    assignment.getDeliveryFee(),
                    TransactionType.ORDER_EARNING,
                    assignment.getId(),
                    "DELIVERY_ASSIGNMENT",
                    "Delivery earning for assignment " + assignment.getDisplayId(),
                    "system"
            );
        } catch (Exception e) {
            // Log but don't fail the status update
        }
    }

    // ─── SubOrder status update ───────────────────────────────────────────────

    private void updateSubOrderStatus(String subOrderId, OrderStatus status) {
        subOrderRepo.findById(subOrderId).ifPresent(so -> {
            so.setStatus(status);
            if (status == OrderStatus.DELIVERED) {
                so.setDeliveredAt(Instant.now());
            }
            subOrderRepo.save(so);
        });
    }

    // ─── Status transition validation ────────────────────────────────────────

    private void validateStatusTransition(DeliveryAssignmentStatus current, DeliveryAssignmentStatus next) {
        boolean valid = switch (current) {
            case ASSIGNED -> next == DeliveryAssignmentStatus.ACCEPTED || next == DeliveryAssignmentStatus.REJECTED;
            case ACCEPTED -> next == DeliveryAssignmentStatus.PICKED_UP || next == DeliveryAssignmentStatus.CANCELLED;
            case PICKED_UP -> next == DeliveryAssignmentStatus.ON_THE_WAY || next == DeliveryAssignmentStatus.DELIVERED;
            case ON_THE_WAY -> next == DeliveryAssignmentStatus.DELIVERED;
            case DELIVERED, CANCELLED, REJECTED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid status transition from " + current + " to " + next);
        }
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    public List<DeliveryAssignment> getForPartner(String partnerId) {
        return assignmentRepo.findAllByDeliveryPartnerIdOrderByCreatedAtDesc(partnerId);
    }

    public List<DeliveryAssignment> getForPartnerByStatus(String partnerId, DeliveryAssignmentStatus status) {
        return assignmentRepo.findAllByDeliveryPartnerIdAndStatusOrderByCreatedAtDesc(partnerId, status);
    }

    public List<DeliveryAssignment> getAll() {
        return assignmentRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<DeliveryAssignment> getAllByStatus(DeliveryAssignmentStatus status) {
        return assignmentRepo.findAllByDeliveryPartnerIdAndStatusOrderByCreatedAtDesc(null, status);
    }

    public DeliveryAssignment getById(String id) {
        return assignmentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
    }

    public DeliveryAssignment getByIdForPartner(String id, String partnerId) {
        return assignmentRepo.findByIdAndDeliveryPartnerId(id, partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
    }

    public Optional<DeliveryAssignment> getBySubOrderId(String subOrderId) {
        return assignmentRepo.findBySubOrderId(subOrderId);
    }
}
