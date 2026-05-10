package com.nainital.backend.wallet.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.wallet.dto.*;
import com.nainital.backend.wallet.model.*;
import com.nainital.backend.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // ─── Seller wallet endpoints ─────────────────────────────────────────────

    @GetMapping("/api/seller/wallet")
    public ResponseEntity<ApiResponse<WalletDto>> sellerWallet(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.ok(toDto(walletService.getWallet(u.getUsername(), WalletOwnerType.SELLER))));
    }

    @GetMapping("/api/seller/wallet/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> sellerTransactions(
            @AuthenticationPrincipal UserDetails u,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                walletService.getTransactionsPaged(u.getUsername(), page, size)
                        .stream().map(this::toTxDto).toList()));
    }

    @GetMapping("/api/seller/wallet/withdrawals")
    public ResponseEntity<ApiResponse<List<WithdrawalRequestDto>>> sellerWithdrawals(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.ok(
                walletService.getWithdrawals(u.getUsername()).stream().map(this::toWdDto).toList()));
    }

    @PostMapping("/api/seller/wallet/withdraw")
    public ResponseEntity<ApiResponse<WithdrawalRequestDto>> requestWithdrawal(
            @AuthenticationPrincipal UserDetails u,
            @RequestBody WithdrawalCreateRequest req) {
        long paise = (long) (req.getAmountRupees() * 100);
        WithdrawalRequest wr = walletService.requestWithdrawal(
                u.getUsername(), WalletOwnerType.SELLER, paise,
                req.getBankAccountNumber(), req.getBankIfsc(),
                req.getBankAccountHolderName(), req.getBankName(), req.getUpiId());
        return ResponseEntity.ok(ApiResponse.ok(toWdDto(wr)));
    }

    // ─── Customer wallet endpoints ────────────────────────────────────────────

    @GetMapping("/api/wallet")
    public ResponseEntity<ApiResponse<WalletDto>> customerWallet(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(ApiResponse.ok(toDto(walletService.getWallet(u.getUsername(), WalletOwnerType.CUSTOMER))));
    }

    @GetMapping("/api/wallet/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> customerTransactions(
            @AuthenticationPrincipal UserDetails u,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                walletService.getTransactionsPaged(u.getUsername(), page, size)
                        .stream().map(this::toTxDto).toList()));
    }

    // ─── Admin wallet endpoints ───────────────────────────────────────────────

    @GetMapping("/api/admin/wallet/platform")
    public ResponseEntity<ApiResponse<WalletDto>> platformWallet() {
        return ResponseEntity.ok(ApiResponse.ok(toDto(walletService.getPlatformWallet())));
    }

    @GetMapping("/api/admin/wallet/sellers")
    public ResponseEntity<ApiResponse<List<WalletDto>>> allSellerWallets() {
        return ResponseEntity.ok(ApiResponse.ok(
                walletService.getAllSellerWallets().stream().map(this::toDto).toList()));
    }

    @GetMapping("/api/admin/wallet/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<WalletDto>> sellerWalletAdmin(@PathVariable String sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(toDto(walletService.getWallet(sellerId, WalletOwnerType.SELLER))));
    }

    @GetMapping("/api/admin/wallet/sellers/{sellerId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> sellerTransactionsAdmin(@PathVariable String sellerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                walletService.getTransactions(sellerId).stream().map(this::toTxDto).toList()));
    }

    @PostMapping("/api/admin/wallet/sellers/{sellerId}/credit")
    public ResponseEntity<ApiResponse<TransactionDto>> adminCreditSeller(
            @PathVariable String sellerId,
            @AuthenticationPrincipal UserDetails admin,
            @RequestBody Map<String, Object> body) {
        double rupees = ((Number) body.get("amountRupees")).doubleValue();
        String note = (String) body.getOrDefault("note", "Manual credit");
        return ResponseEntity.ok(ApiResponse.ok(toTxDto(
                walletService.adminCredit(sellerId, WalletOwnerType.SELLER,
                        (long) (rupees * 100), note, admin.getUsername()))));
    }

    @PostMapping("/api/admin/wallet/sellers/{sellerId}/debit")
    public ResponseEntity<ApiResponse<TransactionDto>> adminDebitSeller(
            @PathVariable String sellerId,
            @AuthenticationPrincipal UserDetails admin,
            @RequestBody Map<String, Object> body) {
        double rupees = ((Number) body.get("amountRupees")).doubleValue();
        String note = (String) body.getOrDefault("note", "Manual debit");
        return ResponseEntity.ok(ApiResponse.ok(toTxDto(
                walletService.adminDebit(sellerId, WalletOwnerType.SELLER,
                        (long) (rupees * 100), note, admin.getUsername()))));
    }

    @PostMapping("/api/admin/wallet/customers/{customerId}/refund")
    public ResponseEntity<ApiResponse<TransactionDto>> refundCustomer(
            @PathVariable String customerId,
            @AuthenticationPrincipal UserDetails admin,
            @RequestBody Map<String, Object> body) {
        int rupees = ((Number) body.get("amountRupees")).intValue();
        String orderId = (String) body.getOrDefault("orderId", "manual");
        return ResponseEntity.ok(ApiResponse.ok(toTxDto(
                walletService.refundToCustomer(customerId, orderId, rupees, admin.getUsername()))));
    }

    // ─── Withdrawal management (Admin) ────────────────────────────────────────

    @GetMapping("/api/admin/withdrawals")
    public ResponseEntity<ApiResponse<List<WithdrawalRequestDto>>> allWithdrawals(
            @RequestParam(required = false) String status) {
        List<WithdrawalRequestDto> list = (status != null && status.equalsIgnoreCase("pending")
                ? walletService.getPendingWithdrawals()
                : walletService.getAllWithdrawals())
                .stream().map(this::toWdDto).toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/api/admin/withdrawals/{id}/approve")
    public ResponseEntity<ApiResponse<WithdrawalRequestDto>> approveWithdrawal(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails admin,
            @RequestBody Map<String, String> body) {
        String utr = body.getOrDefault("utrReference", "");
        return ResponseEntity.ok(ApiResponse.ok(toWdDto(
                walletService.approveWithdrawal(id, admin.getUsername(), utr))));
    }

    @PostMapping("/api/admin/withdrawals/{id}/reject")
    public ResponseEntity<ApiResponse<WithdrawalRequestDto>> rejectWithdrawal(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails admin,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Rejected by admin");
        return ResponseEntity.ok(ApiResponse.ok(toWdDto(
                walletService.rejectWithdrawal(id, admin.getUsername(), reason))));
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private WalletDto toDto(Wallet w) {
        return WalletDto.builder()
                .id(w.getId())
                .ownerId(w.getOwnerId())
                .ownerType(w.getOwnerType().name())
                .balanceRupees(w.getBalance() / 100.0)
                .pendingBalanceRupees(w.getPendingBalance() / 100.0)
                .lifetimeEarnedRupees(w.getLifetimeEarned() / 100.0)
                .lifetimeWithdrawnRupees(w.getLifetimeWithdrawn() / 100.0)
                .active(w.isActive())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    private TransactionDto toTxDto(WalletTransaction t) {
        return TransactionDto.builder()
                .id(t.getId())
                .type(t.getType().name())
                .amountRupees(t.getAmount() / 100.0)
                .balanceAfterRupees(t.getBalanceAfter() / 100.0)
                .referenceId(t.getReferenceId())
                .referenceType(t.getReferenceType())
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private WithdrawalRequestDto toWdDto(WithdrawalRequest w) {
        return WithdrawalRequestDto.builder()
                .id(w.getId())
                .ownerId(w.getOwnerId())
                .ownerType(w.getOwnerType().name())
                .amountRupees(w.getAmount() / 100.0)
                .status(w.getStatus().name())
                .bankAccountNumber(w.getBankAccountNumber())
                .bankIfsc(w.getBankIfsc())
                .bankAccountHolderName(w.getBankAccountHolderName())
                .bankName(w.getBankName())
                .upiId(w.getUpiId())
                .adminNote(w.getAdminNote())
                .utrReference(w.getUtrReference())
                .createdAt(w.getCreatedAt())
                .processedAt(w.getProcessedAt())
                .build();
    }
}
