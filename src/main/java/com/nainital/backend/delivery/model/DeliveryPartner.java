package com.nainital.backend.delivery.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("delivery_partners")
public class DeliveryPartner {

    @Id private String id;

    private String name;

    @Indexed(unique = true)
    private String phone;

    private String email;
    private String profileImage;
    private String vehicleType;   // BIKE | CYCLE | SCOOTER
    private String vehicleNumber;

    @Builder.Default
    private PartnerStatus status = PartnerStatus.PENDING;

    @Builder.Default
    private boolean online = false;

    @Builder.Default
    private boolean active = true;

    private int totalDeliveries;
    private double rating;

    @CreatedDate  private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
