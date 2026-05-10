package com.nainital.backend.wallet.repository;

import com.nainital.backend.wallet.model.WithdrawalRequest;
import com.nainital.backend.wallet.model.WithdrawalStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WithdrawalRequestRepository extends MongoRepository<WithdrawalRequest, String> {
    List<WithdrawalRequest> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId);
    List<WithdrawalRequest> findAllByStatusOrderByCreatedAtDesc(WithdrawalStatus status);
    List<WithdrawalRequest> findAllByOrderByCreatedAtDesc();
    long countByOwnerIdAndStatus(String ownerId, WithdrawalStatus status);
}
