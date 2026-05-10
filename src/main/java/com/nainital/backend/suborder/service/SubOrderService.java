package com.nainital.backend.suborder.service;

import com.nainital.backend.order.model.Order;
import com.nainital.backend.order.model.OrderItem;
import com.nainital.backend.order.model.OrderStatus;
import com.nainital.backend.seller.repository.SellerRepository;
import com.nainital.backend.suborder.model.SubOrder;
import com.nainital.backend.suborder.repository.SubOrderRepository;
import com.nainital.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubOrderService {

    private final SubOrderRepository subOrderRepo;
    private final SellerRepository sellerRepo;
    private final WalletService walletService;

    private static final int DEFAULT_COMMISSION_RATE = 10; // 10%

    // ─── Split master order into sub-orders ──────────────────────────────────

    /**
     * Called right after a master Order is confirmed (COD or paid).
     * Groups order items by storeId and creates one SubOrder per store.
     */
    public List<SubOrder> splitAndCreate(Order masterOrder) {
        if (masterOrder.getItems() == null || masterOrder.getItems().isEmpty()) {
            return List.of();
        }

        // Group items by storeId
        Map<String, List<OrderItem>> byStore = masterOrder.getItems().stream()
                .filter(i -> i.getStoreId() != null)
                .collect(Collectors.groupingBy(OrderItem::getStoreId,
                        LinkedHashMap::new, Collectors.toList()));

        List<SubOrder> subOrders = new ArrayList<>();
        int numStores = byStore.size();

        for (Map.Entry<String, List<OrderItem>> entry : byStore.entrySet()) {
            String storeId = entry.getKey();
            List<OrderItem> items = entry.getValue();

            int subtotal = items.stream().mapToInt(OrderItem::getLineTotal).sum();
            // Distribute delivery fee proportionally
            int deliveryFee = numStores > 1
                    ? Math.round((float) masterOrder.getDeliveryFee() / numStores)
                    : masterOrder.getDeliveryFee();
            int taxes = (int) Math.round(subtotal * 0.05);
            int discount = numStores > 1
                    ? Math.round((float) masterOrder.getDiscount() / numStores)
                    : masterOrder.getDiscount();
            int total = subtotal + deliveryFee + taxes - discount;

            int commissionAmount = total * DEFAULT_COMMISSION_RATE / 100;
            int sellerEarning = total - commissionAmount;

            // Resolve sellerId from storeId
            String sellerId = sellerRepo.findAll().stream()
                    .filter(s -> storeId.equals(s.getStoreId()))
                    .map(s -> s.getId())
                    .findFirst()
                    .orElse(null);

            String displayId = "SUB-" + generateShortId();

            SubOrder sub = SubOrder.builder()
                    .displayId(displayId)
                    .masterOrderId(masterOrder.getId())
                    .sellerId(sellerId)
                    .storeId(storeId)
                    .customerId(masterOrder.getUserId())
                    .items(items)
                    .subtotal(subtotal)
                    .deliveryFee(deliveryFee)
                    .taxes(taxes)
                    .discount(discount)
                    .total(total)
                    .addressSnapshot(masterOrder.getAddressSnapshot())
                    .paymentMode(masterOrder.getPaymentMode())
                    .paid(masterOrder.getPaymentMode().equals("cod") || masterOrder.getStatus() == OrderStatus.PAID)
                    .status(masterOrder.getStatus())
                    .commissionRate(DEFAULT_COMMISSION_RATE)
                    .commissionAmount(commissionAmount)
                    .sellerEarning(sellerEarning)
                    .earningCredited(false)
                    .build();

            subOrders.add(subOrderRepo.save(sub));
        }

        return subOrders;
    }

    // ─── Update sub-order status ──────────────────────────────────────────────

    public SubOrder updateStatus(String subOrderId, String sellerId, OrderStatus newStatus) {
        SubOrder sub = subOrderRepo.findByIdAndSellerId(subOrderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-order not found"));
        sub.setStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            sub.setDeliveredAt(Instant.now());
            // Credit seller wallet if not already done
            if (!sub.isEarningCredited() && sub.getSellerId() != null) {
                walletService.settleSubOrderEarning(
                        sub.getSellerId(), sub.getId(),
                        sub.getSellerEarning(), sub.getCommissionRate());
                sub.setEarningCredited(true);
            }
        }
        return subOrderRepo.save(sub);
    }

    // Admin override — can update any sub-order
    public SubOrder adminUpdateStatus(String subOrderId, OrderStatus newStatus) {
        SubOrder sub = subOrderRepo.findById(subOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-order not found"));
        sub.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            sub.setDeliveredAt(Instant.now());
            if (!sub.isEarningCredited() && sub.getSellerId() != null) {
                walletService.settleSubOrderEarning(
                        sub.getSellerId(), sub.getId(),
                        sub.getSellerEarning(), sub.getCommissionRate());
                sub.setEarningCredited(true);
            }
        }
        return subOrderRepo.save(sub);
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    public List<SubOrder> getForSeller(String sellerId) {
        return subOrderRepo.findAllBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    public List<SubOrder> getForMasterOrder(String masterOrderId) {
        return subOrderRepo.findAllByMasterOrderIdOrderByCreatedAtDesc(masterOrderId);
    }

    public List<SubOrder> getForCustomer(String customerId) {
        return subOrderRepo.findAllByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public SubOrder getById(String id) {
        return subOrderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sub-order not found"));
    }

    public SubOrder getByIdForSeller(String id, String sellerId) {
        return subOrderRepo.findByIdAndSellerId(id, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-order not found"));
    }

    public List<SubOrder> getAll() {
        return subOrderRepo.findAll();
    }

    private String generateShortId() {
        return Long.toString(System.currentTimeMillis() % 1_000_000_000L, 36).toUpperCase();
    }
}
