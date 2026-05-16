package com.nainital.backend.delivery.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.delivery.dto.DeliveryAssignmentDto;
import com.nainital.backend.delivery.model.DeliveryAssignment;
import com.nainital.backend.delivery.model.DeliveryAssignmentStatus;
import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.service.DeliveryAssignmentService;
import com.nainital.backend.delivery.service.DeliveryPartnerService;
import com.nainital.backend.wallet.model.Wallet;
import com.nainital.backend.wallet.model.WalletOwnerType;
import com.nainital.backend.wallet.model.WalletTransaction;
import com.nainital.backend.wallet.model.WithdrawalRequest;
import com.nainital.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryAssignmentService assignmentService;
    private final DeliveryPartnerService partnerService;
    private final WalletService walletService;

    // ─── Assignments ──────────────────────────────────────────────────────────

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<DeliveryAssignmentDto>>> getMyAssignments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status) {
        String partnerId = userDetails.getUsername();
        List<DeliveryAssignment> assignments;
        if (status != null && !status.isBlank()) {
            DeliveryAssignmentStatus st = DeliveryAssignmentStatus.valueOf(status.toUpperCase());
            assignments = assignmentService.getForPartnerByStatus(partnerId, st);
        } else {
            assignments = assignmentService.getForPartner(partnerId);
        }
        return ResponseEntity.ok(ApiResponse.ok(assignments.stream()
                .map(this::toDto)
                .collect(Collectors.toList())));
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<ApiResponse<DeliveryAssignmentDto>> getAssignment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        String partnerId = userDetails.getUsername();
        DeliveryAssignment assignment = assignmentService.getByIdForPartner(id, partnerId);
        return ResponseEntity.ok(ApiResponse.ok(toDto(assignment)));
    }

    @PatchMapping("/assignments/{id}/status")
    public ResponseEntity<ApiResponse<DeliveryAssignmentDto>> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String partnerId = userDetails.getUsername();
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("status is required"));
        }
        DeliveryAssignmentStatus newStatus = DeliveryAssignmentStatus.valueOf(statusStr.toUpperCase());
        DeliveryAssignment updated = assignmentService.updateStatus(id, partnerId, newStatus);
        return ResponseEntity.ok(ApiResponse.ok("Status updated", toDto(updated)));
    }

    // ─── Wallet ───────────────────────────────────────────────────────────────

    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<Wallet>> getWallet(
            @AuthenticationPrincipal UserDetails userDetails) {
        Wallet wallet = walletService.getWallet(userDetails.getUsername(), WalletOwnerType.DELIVERY_PARTNER);
        return ResponseEntity.ok(ApiResponse.ok(wallet));
    }

    @GetMapping("/wallet/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<WalletTransaction> txs = walletService.getTransactionsPaged(
                userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(txs));
    }

    @GetMapping("/wallet/withdrawals")
    public ResponseEntity<ApiResponse<List<WithdrawalRequest>>> getWithdrawals(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<WithdrawalRequest> withdrawals = walletService.getWithdrawals(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(withdrawals));
    }

    @PostMapping("/wallet/withdraw")
    public ResponseEntity<ApiResponse<WithdrawalRequest>> requestWithdrawal(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        String partnerId = userDetails.getUsername();
        DeliveryPartner partner = partnerService.getProfile(partnerId);

        long amountPaise = Long.parseLong(body.getOrDefault("amountPaise", "0").toString());
        String bankAccount = (String) body.getOrDefault("bankAccountNumber", partner.getBankAccountNumber());
        String bankIfsc = (String) body.getOrDefault("bankIfsc", partner.getBankIfsc());
        String bankHolderName = (String) body.getOrDefault("bankAccountHolderName", partner.getBankAccountHolderName());
        String bankName = (String) body.getOrDefault("bankName", partner.getBankName());
        String upiId = (String) body.getOrDefault("upiId", partner.getUpiId());

        WithdrawalRequest wr = walletService.requestWithdrawal(
                partnerId, WalletOwnerType.DELIVERY_PARTNER,
                amountPaise, bankAccount, bankIfsc, bankHolderName, bankName, upiId);
        return ResponseEntity.ok(ApiResponse.ok("Withdrawal request submitted", wr));
    }

    // ─── Push (FCM) ───────────────────────────────────────────────────────────

    @PostMapping("/push-token")
    public ResponseEntity<ApiResponse<String>> registerPushToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("token is required"));
        }
        partnerService.registerFcmToken(userDetails.getUsername(), token);
        return ResponseEntity.ok(ApiResponse.ok("Push token registered", "ok"));
    }

    @DeleteMapping("/push-token")
    public ResponseEntity<ApiResponse<String>> removePushToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null && !token.isBlank()) {
            partnerService.removeFcmToken(userDetails.getUsername(), token);
        }
        return ResponseEntity.ok(ApiResponse.ok("Push token removed", "ok"));
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        String partnerId = userDetails.getUsername();
        List<DeliveryAssignment> all = assignmentService.getForPartner(partnerId);

        long activeCount = all.stream()
                .filter(a -> a.getStatus() == DeliveryAssignmentStatus.ASSIGNED
                        || a.getStatus() == DeliveryAssignmentStatus.ACCEPTED
                        || a.getStatus() == DeliveryAssignmentStatus.PICKED_UP
                        || a.getStatus() == DeliveryAssignmentStatus.ON_THE_WAY)
                .count();

        long completedCount = all.stream()
                .filter(a -> a.getStatus() == DeliveryAssignmentStatus.DELIVERED)
                .count();

        // Today's range in IST
        ZonedDateTime startOfToday = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .toLocalDate().atStartOfDay(ZoneId.of("Asia/Kolkata"));
        Instant todayStart = startOfToday.toInstant();

        long earningsToday = all.stream()
                .filter(a -> a.getStatus() == DeliveryAssignmentStatus.DELIVERED
                        && a.getDeliveredAt() != null
                        && a.getDeliveredAt().isAfter(todayStart))
                .mapToLong(DeliveryAssignment::getDeliveryFee)
                .sum();

        long totalEarnings = all.stream()
                .filter(a -> a.getStatus() == DeliveryAssignmentStatus.DELIVERED)
                .mapToLong(DeliveryAssignment::getDeliveryFee)
                .sum();

        Wallet wallet = walletService.getWallet(partnerId, WalletOwnerType.DELIVERY_PARTNER);

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "activeDeliveries", activeCount,
                "completedDeliveries", completedCount,
                "earningsTodayPaise", earningsToday,
                "totalEarningsPaise", totalEarnings,
                "walletBalancePaise", wallet.getBalance()
        )));
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
