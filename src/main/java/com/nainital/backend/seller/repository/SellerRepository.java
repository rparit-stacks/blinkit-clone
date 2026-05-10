package com.nainital.backend.seller.repository;

import com.nainital.backend.seller.model.Seller;
import com.nainital.backend.seller.model.SellerStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends MongoRepository<Seller, String> {
    Optional<Seller> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Seller> findAllByStatusOrderByCreatedAtDesc(SellerStatus status);
    List<Seller> findAllByOrderByCreatedAtDesc();
}
