package com.nainital.backend.wallet.service;

import com.nainital.backend.wallet.model.*;
import com.nainital.backend.wallet.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepo;
    private final WalletTransactionRepository txRepo;
    private final WithdrawalRequestRepository withdrawalRepo;

    // Default platform commission (10%)
    private static final int DEFAULT_COMMISSION_RATE = 10;
    private static final String PLATFORM_OWNER_ID = "platform";

    // ─── Wallet bootstrap ────────────────────────────────────────────────────

    public Wallet getOrCreate(String ownerId, WalletOwnerType type) {
        return walletRepo.findByOwnerIdAndOwnerType(ownerId, type)
                .orElseGet(() -> walletRepo.save(Wallet.builder()
                        .ownerId(ownerId)
                        .ownerType(type)
                        .balance(0)
                        .pendingBalance(0)
                        .lifetimeEarned(0)
                        .lifetimeWithdrawn(0)
                        .active(true)
                        .build()));
    }

    // ─── Credit / Debit ──────────────────────────────────────────────────────

    public WalletTransaction credit(String ownerId, WalletOwnerType type,
                                    long amountPaise, TransactionType txType,
                                    String referenceId, String referenceType,
                                    String note, String initiatedBy) {
        Wallet wallet = getOrCreate(ownerId, type);
        wallet.setBalance(wallet.getBalance() + amountPaise);
        wallet.setLifetimeEarned(wallet.getLifetimeEarned() + amountPaise);
        walletRepo.save(wallet);

        return txRepo.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .ownerId(ownerId)
                .ownerType(type)
                .type(txType)
                .amount(amountPaise)
                .balanceAfter(wallet.getBalance())
                .referenceId(referenceId)
                .referenceType(referenceType)
                .note(note)
                .initiatedBy(initiatedBy)
                .build());
    }

    public WalletTransaction debit(String ownerId, WalletOwnerType type,
                                   long amountPaise, TransactionType txType,
                                   String referenceId, String referenceType,
                                   String note, String initiatedBy) {
        Wallet wallet = getOrCreate(ownerId, type);
        if (wallet.getBalance() < amountPaise) {
            throw new IllegalStateException("Insufficient wallet balance");
        }
        wallet.setBalance(wallet.getBalance() - amountPaise);
        walletRepo.save(wallet);

        return txRepo.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .ownerId(ownerId)
                .ownerType(type)
                .type(txType)
                .amount(amountPaise)
                .balanceAfter(wallet.getBalance())
                .referenceId(referenceId)
                .referenceType(referenceType)
                .note(note)
                .initiatedBy(initiatedBy)
                .build());
    }

    // ─── Order earning settlement ─────────────────────────────────────────────

    /**
     * Called when a sub-order is DELIVERED.
     * Splits the order total into platform commission and seller earning.
     * Credits both wallets.
     */
    public Map<String, Long> settleSubOrderEarning(String sellerId, String subOrderId,
                                                    int subOrderTotal, int commissionRate) {
        int commissionPaise = (subOrderTotal * 100L * commissionRate / 100L > Integer.MAX_VALUE)
                ? subOrderTotal * commissionRate / 100
                : subOrderTotal * commissionRate / 100;
        int sellerEarningPaise = subOrderTotal - commissionPaise;

        // Credit seller wallet
        credit(sellerId, WalletOwnerType.SELLER,
                sellerEarningPaise * 100L,
                TransactionType.ORDER_EARNING,
                subOrderId, "SUB_ORDER",
                "Earnings for sub-order " + subOrderId,
                "system");

        // Credit platform wallet
        credit(PLATFORM_OWNER_ID, WalletOwnerType.ADMIN,
                commissionPaise * 100L,
                TransactionType.COMMISSION,
                subOrderId, "SUB_ORDER",
                "Commission from sub-order " + subOrderId,
                "system");

        return Map.of(
                "sellerEarning", (long) sellerEarningPaise,
                "commission", (long) commissionPaise
        );
    }

    // ─── Refund to customer wallet ────────────────────────────────────────────

    public WalletTransaction refundToCustomer(String customerId, String orderId,
                                               int refundAmountRupees, String adminId) {
        return credit(customerId, WalletOwnerType.CUSTOMER,
                refundAmountRupees * 100L,
                TransactionType.REFUND,
                orderId, "ORDER",
                "Refund for order " + orderId,
                "admin:" + adminId);
    }

    // ─── Withdrawal ──────────────────────────────────────────────────────────

    public WithdrawalRequest requestWithdrawal(String ownerId, WalletOwnerType ownerType,
                                                long amountPaise,
                                                String bankAccount, String bankIfsc,
                                                String bankHolderName, String bankName,
                                                String upiId) {
        Wallet wallet = getOrCreate(ownerId, ownerType);
        if (wallet.getBalance() < amountPaise) {
            throw new IllegalStateException("Withdrawal amount exceeds available balance");
        }
        if (amountPaise < 100_00) { // ₹100 minimum
            throw new IllegalArgumentException("Minimum withdrawal is ₹100");
        }

        // Freeze the amount (deduct from balance, pending processing)
        wallet.setBalance(wallet.getBalance() - amountPaise);
        walletRepo.save(wallet);

        // Log as debit (pending)
        txRepo.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .ownerId(ownerId)
                .ownerType(ownerType)
                .type(TransactionType.WITHDRAWAL)
                .amount(amountPaise)
                .balanceAfter(wallet.getBalance())
                .referenceType("WITHDRAWAL_REQUEST")
                .note("Withdrawal request submitted")
                .initiatedBy(ownerId)
                .build());

        return withdrawalRepo.save(WithdrawalRequest.builder()
                .ownerId(ownerId)
                .ownerType(ownerType)
                .amount(amountPaise)
                .status(WithdrawalStatus.PENDING)
                .bankAccountNumber(bankAccount)
                .bankIfsc(bankIfsc)
                .bankAccountHolderName(bankHolderName)
                .bankName(bankName)
                .upiId(upiId)
                .build());
    }

    public WithdrawalRequest approveWithdrawal(String withdrawalId, String adminId, String utrReference) {
        WithdrawalRequest wr = getWithdrawal(withdrawalId);
        if (wr.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Withdrawal is not pending");
        }
        wr.setStatus(WithdrawalStatus.PROCESSED);
        wr.setProcessedBy(adminId);
        wr.setProcessedAt(Instant.now());
        wr.setUtrReference(utrReference);

        // Update wallet lifetime withdrawn
        Wallet wallet = getOrCreate(wr.getOwnerId(), wr.getOwnerType());
        wallet.setLifetimeWithdrawn(wallet.getLifetimeWithdrawn() + wr.getAmount());
        walletRepo.save(wallet);

        // Log
        txRepo.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .ownerId(wr.getOwnerId())
                .ownerType(wr.getOwnerType())
                .type(TransactionType.SETTLEMENT)
                .amount(wr.getAmount())
                .balanceAfter(wallet.getBalance())
                .referenceId(withdrawalId)
                .referenceType("WITHDRAWAL_REQUEST")
                .note("Withdrawal processed by admin. UTR: " + utrReference)
                .initiatedBy("admin:" + adminId)
                .build());

        return withdrawalRepo.save(wr);
    }

    public WithdrawalRequest rejectWithdrawal(String withdrawalId, String adminId, String reason) {
        WithdrawalRequest wr = getWithdrawal(withdrawalId);
        if (wr.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Withdrawal is not pending");
        }
        wr.setStatus(WithdrawalStatus.REJECTED);
        wr.setProcessedBy(adminId);
        wr.setProcessedAt(Instant.now());
        wr.setAdminNote(reason);

        // Refund frozen amount back to wallet
        Wallet wallet = getOrCreate(wr.getOwnerId(), wr.getOwnerType());
        wallet.setBalance(wallet.getBalance() + wr.getAmount());
        walletRepo.save(wallet);

        // Log reversal
        txRepo.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .ownerId(wr.getOwnerId())
                .ownerType(wr.getOwnerType())
                .type(TransactionType.MANUAL_CREDIT)
                .amount(wr.getAmount())
                .balanceAfter(wallet.getBalance())
                .referenceId(withdrawalId)
                .referenceType("WITHDRAWAL_REVERSAL")
                .note("Withdrawal rejected, amount returned. Reason: " + reason)
                .initiatedBy("admin:" + adminId)
                .build());

        return withdrawalRepo.save(wr);
    }

    // ─── Admin manual adjust ─────────────────────────────────────────────────

    public WalletTransaction adminCredit(String ownerId, WalletOwnerType ownerType,
                                          long amountPaise, String note, String adminId) {
        return credit(ownerId, ownerType, amountPaise, TransactionType.MANUAL_CREDIT,
                null, "MANUAL", note, "admin:" + adminId);
    }

    public WalletTransaction adminDebit(String ownerId, WalletOwnerType ownerType,
                                         long amountPaise, String note, String adminId) {
        return debit(ownerId, ownerType, amountPaise, TransactionType.MANUAL_DEBIT,
                null, "MANUAL", note, "admin:" + adminId);
    }

    // ─── Queries ─────────────────────────────────────────────────────────────

    public Wallet getWallet(String ownerId, WalletOwnerType type) {
        return getOrCreate(ownerId, type);
    }

    public List<WalletTransaction> getTransactions(String ownerId) {
        return txRepo.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public List<WalletTransaction> getTransactionsPaged(String ownerId, int page, int size) {
        return txRepo.findAllByOwnerIdOrderByCreatedAtDesc(ownerId, PageRequest.of(page, size)).getContent();
    }

    public List<WithdrawalRequest> getWithdrawals(String ownerId) {
        return withdrawalRepo.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public List<WithdrawalRequest> getAllWithdrawals() {
        return withdrawalRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<WithdrawalRequest> getPendingWithdrawals() {
        return withdrawalRepo.findAllByStatusOrderByCreatedAtDesc(WithdrawalStatus.PENDING);
    }

    public List<Wallet> getAllSellerWallets() {
        return walletRepo.findAllByOwnerType(WalletOwnerType.SELLER);
    }

    public List<Wallet> getAllCustomerWallets() {
        return walletRepo.findAllByOwnerType(WalletOwnerType.CUSTOMER);
    }

    public List<Wallet> getAllByOwnerType(WalletOwnerType type) {
        return walletRepo.findAllByOwnerType(type);
    }

    public List<Wallet> getAllDeliveryPartnerWallets() {
        return walletRepo.findAllByOwnerType(WalletOwnerType.DELIVERY_PARTNER);
    }

    public Wallet getPlatformWallet() {
        return getOrCreate(PLATFORM_OWNER_ID, WalletOwnerType.ADMIN);
    }

    private WithdrawalRequest getWithdrawal(String id) {
        return withdrawalRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal request not found"));
    }
}
