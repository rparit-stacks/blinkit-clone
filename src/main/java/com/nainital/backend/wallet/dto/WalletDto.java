package com.nainital.backend.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WalletDto {
    private String id;
    private String ownerId;
    private String ownerType;
    private double balanceRupees;
    private double pendingBalanceRupees;
    private double lifetimeEarnedRupees;
    private double lifetimeWithdrawnRupees;
    private boolean active;
    private Instant updatedAt;
}
