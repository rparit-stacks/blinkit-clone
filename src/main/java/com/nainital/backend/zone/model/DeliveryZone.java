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

    // Delivery rate for this zone
    private int deliveryFee;           // e.g. 30 (rupees)
    private int minOrderForFree;       // e.g. 500 — 0 means never free

    // Tax rate for this zone (overrides global default when non-zero)
    // e.g. 0.05 = 5%, 0.18 = 18% GST, 0.0 = use global default
    private double taxRate;            // 0.0 means "use global default"

    // Estimated delivery time label e.g. "6-10 mins"
    private String etaLabel;

    // Polygon boundary: list of [lng, lat] pairs (GeoJSON order)
    // If null/empty and active=true, zone matches everywhere (global zone)
    private List<double[]> polygon;

    // If true, this zone applies to entire serviceable area (no polygon needed)
    private boolean everywhere;
}
