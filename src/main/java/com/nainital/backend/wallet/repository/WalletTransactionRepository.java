package com.nainital.backend.wallet.repository;

import com.nainital.backend.wallet.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WalletTransactionRepository extends MongoRepository<WalletTransaction, String> {
    List<WalletTransaction> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId);
    Page<WalletTransaction> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId, Pageable pageable);
    List<WalletTransaction> findAllByWalletIdOrderByCreatedAtDesc(String walletId);
    long countByOwnerId(String ownerId);
}
