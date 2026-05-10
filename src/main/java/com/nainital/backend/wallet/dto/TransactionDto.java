package com.nainital.backend.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionDto {
    private String id;
    private String type;
    private double amountRupees;
    private double balanceAfterRupees;
    private String referenceId;
    private String referenceType;
    private String note;
    private Instant createdAt;
}
