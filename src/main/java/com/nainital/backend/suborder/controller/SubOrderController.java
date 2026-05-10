package com.nainital.backend.suborder.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.order.model.OrderStatus;
import com.nainital.backend.suborder.dto.SubOrderDto;
import com.nainital.backend.suborder.model.SubOrder;
import com.nainital.backend.suborder.service.SubOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SubOrderController {

    private final SubOrderService subOrderService;

    // ─── Seller endpoints ─────────────────────────────────────────────────────

    @GetMapping("/api/seller/orders")
    public ResponseEntity<ApiResponse<List<SubOrderDto>>> sellerOrders(
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.ok(
                subOrderService.getForSeller(u.getUsername()).stream().map(this::toDto).toList()));
    }

    @GetMapping("/api/seller/orders/{id}")
    public ResponseEntity<ApiResponse<SubOrderDto>> sellerOrder(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.ok(
                toDto(subOrderService.getByIdForSeller(id, u.getUsername()))));
    }

    @PatchMapping("/api/seller/orders/{id}/status")
    public ResponseEntity<ApiResponse<SubOrderDto>> updateStatus(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails u,
            @RequestBody Map<String, String> body) {
        OrderStatus newStatus = OrderStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(
                toDto(subOrderService.updateStatus(id, u.getUsername(), newStatus))));
    }

    // ─── Admin endpoints ──────────────────────────────────────────────────────

    @GetMapping("/api/admin/sub-orders")
    public ResponseEntity<ApiResponse<List<SubOrderDto>>> allSubOrders(
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) String masterOrderId) {
        List<SubOrder> list;
        if (sellerId != null) {
            list = subOrderService.getForSeller(sellerId);
        } else if (masterOrderId != null) {
            list = subOrderService.getForMasterOrder(masterOrderId);
        } else {
            list = subOrderService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.ok(list.stream().map(this::toDto).toList()));
    }

    @GetMapping("/api/admin/sub-orders/{id}")
    public ResponseEntity<ApiResponse<SubOrderDto>> getSubOrder(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(toDto(subOrderService.getById(id))));
    }

    @PatchMapping("/api/admin/sub-orders/{id}/status")
    public ResponseEntity<ApiResponse<SubOrderDto>> adminUpdateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        OrderStatus newStatus = OrderStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(
                toDto(subOrderService.adminUpdateStatus(id, newStatus))));
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private SubOrderDto toDto(SubOrder s) {
        return SubOrderDto.builder()
                .id(s.getId())
                .displayId(s.getDisplayId())
                .masterOrderId(s.getMasterOrderId())
                .sellerId(s.getSellerId())
                .storeId(s.getStoreId())
                .customerId(s.getCustomerId())
                .items(s.getItems())
                .subtotal(s.getSubtotal())
                .deliveryFee(s.getDeliveryFee())
                .taxes(s.getTaxes())
                .discount(s.getDiscount())
                .total(s.getTotal())
                .addressSnapshot(s.getAddressSnapshot())
                .paymentMode(s.getPaymentMode())
                .paid(s.isPaid())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .commissionRate(s.getCommissionRate())
                .commissionAmount(s.getCommissionAmount())
                .sellerEarning(s.getSellerEarning())
                .earningCredited(s.isEarningCredited())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .deliveredAt(s.getDeliveredAt())
                .build();
    }
}
