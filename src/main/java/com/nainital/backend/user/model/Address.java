package com.nainital.backend.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String label;

    private String line1;

    private String line2;

    private String city;

    private String state;

    private String pincode;

    @Builder.Default
    private boolean defaultAddress = false;

    @CreatedDate
    private Instant createdAt;
}
