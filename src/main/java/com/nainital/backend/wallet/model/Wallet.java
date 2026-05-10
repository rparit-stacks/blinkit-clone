package com.nainital.backend.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("wallets")
@CompoundIndexes({
    @CompoundIndex(name = "owner_unique", def = "{'ownerId': 1, 'ownerType': 1}", unique = true)
})
public class Wallet {

    @Id
    private String id;

    private String ownerId;          // userId / sellerId / "platform"
    private WalletOwnerType ownerType;

    private long balance;            // in paise (₹1 = 100 paise) — avoids float issues
    private long pendingBalance;     // earnings not yet settled/withdrawable
    private long lifetimeEarned;     // cumulative credited
    private long lifetimeWithdrawn;  // cumulative withdrawn

    private boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
