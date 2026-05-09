package com.nainital.backend.zone.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("delivery_zones")
public class DeliveryZone {

    @Id
    private String id;

    private String name;
    private boolean active;

    // Polygon boundary: list of [lng, lat] pairs (GeoJSON order)
    private List<double[]> polygon;

    // Estimated delivery time label e.g. "6-10 mins"
    private String etaLabel;
}
