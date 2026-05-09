package com.nainital.backend.admin.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("admins")
public class Admin {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String name;

    @Builder.Default
    private AdminRole role = AdminRole.SUPPORT;

    @Builder.Default
    private boolean active = true;

    @CreatedDate  private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
