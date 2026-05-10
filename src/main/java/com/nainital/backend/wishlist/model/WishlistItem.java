package com.nainital.backend.wishlist.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("wishlist_items")
@CompoundIndex(name = "user_product_unique", def = "{'userId': 1, 'productId': 1}", unique = true)
public class WishlistItem {
    @Id private String id;
    private String userId;
    private String productId;
    @CreatedDate private Instant createdAt;
}
