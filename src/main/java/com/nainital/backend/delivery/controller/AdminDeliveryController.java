package com.nainital.backend.delivery.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.delivery.dto.AssignDeliveryRequest;
import com.nainital.backend.delivery.dto.DeliveryAssignmentDto;
import com.nainital.backend.delivery.model.DeliveryAssignment;
import com.nainital.backend.delivery.model.DeliveryAssignmentStatus;
import com.nainital.backend.delivery.service.DeliveryAssignmentService;
import com.nainital.backend.wallet.model.Wallet;
import com.nainital.backend.wallet.model.WalletOwnerType;
import com.nainital.backend.wallet.model.WalletTransaction;
import com.nainital.backend.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final DeliveryAssignmentService assignmentService;
    private final WalletService walletService;

    // ─── Assignment management ────────────────────────────────────────────────

    @PostMapping("/delivery/assign")
    public ResponseEntity<ApiResponse<DeliveryAssignmentDto>> assign(
            @Valid @RequestBody AssignDeliveryRequest req) {
        DeliveryAssignment assignment = assignmentService.assign(
                req.getSubOrderId(), req.getDeliveryPartnerId(), req.getDeliveryFee());
        return ResponseEntity.ok(ApiResponse.ok("Order assigned to delivery partner", toDto(assignment)));
    }

    @GetMapping("/delivery/assignments")
    public ResponseEntity<ApiResponse<List<DeliveryAssignmentDto>>> getAllAssignments(
            @RequestParam(required = false) String status) {
        List<DeliveryAssignment> assignments;
        if (status != null && !status.isBlank()) {
            DeliveryAssignmentStatus st = DeliveryAssignmentStatus.valueOf(status.toUpperCase());
            assignments = assignmentService.getAll().stream()
                    .filter(a -> a.getStatus() == st)
                    .collect(Collectors.toList());
        } else {
            assignments = assignmentService.getAll();
        }
        return ResponseEntity.ok(ApiResponse.ok(assignments.stream()
                .map(this::toDto)
                .collect(Collectors.toList())));
    }

    @PatchMapping("/delivery/assignments/{id}/status")
    public ResponseEntity<ApiResponse<DeliveryAssignmentDto>> adminUpdateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("status is required"));
        }
        DeliveryAssignmentStatus newStatus = DeliveryAssignmentStatus.valueOf(statusStr.toUpperCase());
        DeliveryAssignment updated = assignmentService.adminUpdateStatus(id, newStatus);
        return ResponseEntity.ok(ApiResponse.ok("Assignment status updated", toDto(updated)));
    }

    // ─── Delivery partner wallets ─────────────────────────────────────────────

    @GetMapping("/wallet/delivery")
    public ResponseEntity<ApiResponse<List<Wallet>>> getAllDeliveryWallets() {
        List<Wallet> wallets = walletService.getAllByOwnerType(WalletOwnerType.DELIVERY_PARTNER);
        return ResponseEntity.ok(ApiResponse.ok(wallets));
    }

    @PostMapping("/wallet/delivery/{partnerId}/credit")
    public ResponseEntity<ApiResponse<WalletTransaction>> creditPartnerWallet(
            @PathVariable String partnerId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        long amountPaise = Long.parseLong(body.getOrDefault("amountPaise", "0").toString());
        String note = (String) body.getOrDefault("note", "Admin credit");
        String adminId = userDetails.getUsername();
        WalletTransaction tx = walletService.adminCredit(
                partnerId, WalletOwnerType.DELIVERY_PARTNER, amountPaise, note, adminId);
        return ResponseEntity.ok(ApiResponse.ok("Credit applied", tx));
    }

    @PostMapping("/wallet/delivery/{partnerId}/debit")
    public ResponseEntity<ApiResponse<WalletTransaction>> debitPartnerWallet(
            @PathVariable String partnerId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        long amountPaise = Long.parseLong(body.getOrDefault("amountPaise", "0").toString());
        String note = (String) body.getOrDefault("note", "Admin debit");
        String adminId = userDetails.getUsername();
        WalletTransaction tx = walletService.adminDebit(
                partnerId, WalletOwnerType.DELIVERY_PARTNER, amountPaise, note, adminId);
        return ResponseEntity.ok(ApiResponse.ok("Debit applied", tx));
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private DeliveryAssignmentDto toDto(DeliveryAssignment a) {
        DeliveryAssignmentDto dto = new DeliveryAssignmentDto();
        dto.setId(a.getId());
        dto.setDisplayId(a.getDisplayId());
        dto.setSubOrderId(a.getSubOrderId());
        dto.setMasterOrderId(a.getMasterOrderId());
        dto.setDeliveryPartnerId(a.getDeliveryPartnerId());
        dto.setSellerId(a.getSellerId());
        dto.setStoreId(a.getStoreId());
        dto.setCustomerId(a.getCustomerId());
        dto.setPickupAddress(a.getPickupAddress());
        dto.setDeliveryAddress(a.getDeliveryAddress());
        dto.setSellerStoreName(a.getSellerStoreName());
        dto.setSellerPhone(a.getSellerPhone());
        dto.setCustomerName(a.getCustomerName());
        dto.setCustomerPhone(a.getCustomerPhone());
        dto.setOrderSummary(a.getOrderSummary());
        dto.setPaymentMode(a.getPaymentMode());
        dto.setPaid(a.isPaid());
        dto.setOrderTotal(a.getOrderTotal());
        dto.setDeliveryFee(a.getDeliveryFee());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        dto.setAssignedAt(a.getAssignedAt());
        dto.setPickedUpAt(a.getPickedUpAt());
        dto.setDeliveredAt(a.getDeliveredAt());
        dto.setCancelledAt(a.getCancelledAt());
        dto.setCancelReason(a.getCancelReason());
        dto.setNotes(a.getNotes());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
