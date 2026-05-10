package com.nainital.backend.seller.repository;

import com.nainital.backend.seller.model.SellerProduct;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SellerProductRepository extends MongoRepository<SellerProduct, String> {
    List<SellerProduct> findAllBySellerIdOrderByCreatedAtDesc(String sellerId);
    Optional<SellerProduct> findByIdAndSellerId(String id, String sellerId);
    void deleteByIdAndSellerId(String id, String sellerId);
    long countBySellerId(String sellerId);
    long countBySellerIdAndAvailable(String sellerId, boolean available);
}
