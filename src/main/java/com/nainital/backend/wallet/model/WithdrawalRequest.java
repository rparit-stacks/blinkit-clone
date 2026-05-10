package com.nainital.backend.wallet.model;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("withdrawal_requests")
public class WithdrawalRequest {

    @Id
    private String id;

    @Indexed
    private String ownerId;
    private WalletOwnerType ownerType;

    private long amount;           // paise requested
    private WithdrawalStatus status;

    // Bank details snapshot at request time
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountHolderName;
    private String bankName;
    private String upiId;          // optional UPI alternative

    // Admin action
    private String adminNote;
    private String processedBy;    // admin id
    private Instant processedAt;

    // UTR / transaction reference from bank transfer
    private String utrReference;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
