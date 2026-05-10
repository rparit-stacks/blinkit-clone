package com.nainital.backend.wishlist.repository;

import com.nainital.backend.wishlist.model.WishlistItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends MongoRepository<WishlistItem, String> {
    List<WishlistItem> findAllByUserIdOrderByCreatedAtDesc(String userId);
    Optional<WishlistItem> findByUserIdAndProductId(String userId, String productId);
    boolean existsByUserIdAndProductId(String userId, String productId);
    void deleteByUserIdAndProductId(String userId, String productId);
    long countByUserId(String userId);
}
