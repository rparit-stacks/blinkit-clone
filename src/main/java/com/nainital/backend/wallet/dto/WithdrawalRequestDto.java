package com.nainital.backend.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WithdrawalRequestDto {
    private String id;
    private String ownerId;
    private String ownerType;
    private double amountRupees;
    private String status;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountHolderName;
    private String bankName;
    private String upiId;
    private String adminNote;
    private String utrReference;
    private Instant createdAt;
    private Instant processedAt;
}
