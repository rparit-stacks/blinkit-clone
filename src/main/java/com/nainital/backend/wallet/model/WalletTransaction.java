package com.nainital.backend.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("wallet_transactions")
public class WalletTransaction {

    @Id
    private String id;

    @Indexed
    private String walletId;

    @Indexed
    private String ownerId;
    private WalletOwnerType ownerType;

    private TransactionType type;
    private long amount;          // paise, always positive
    private long balanceAfter;    // snapshot of balance after this tx

    // Reference context
    private String referenceId;   // orderId / subOrderId / withdrawalId / etc.
    private String referenceType; // "ORDER" | "SUB_ORDER" | "WITHDRAWAL" | "MANUAL"
    private String note;          // human-readable description

    // For audit
    private String initiatedBy;   // userId who triggered (or "system" / "admin:{adminId}")

    @CreatedDate
    private Instant createdAt;
}
