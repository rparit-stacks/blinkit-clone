package com.nainital.backend.wallet.repository;

import com.nainital.backend.wallet.model.Wallet;
import com.nainital.backend.wallet.model.WalletOwnerType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends MongoRepository<Wallet, String> {
    Optional<Wallet> findByOwnerIdAndOwnerType(String ownerId, WalletOwnerType ownerType);
    List<Wallet> findAllByOwnerType(WalletOwnerType ownerType);
}
