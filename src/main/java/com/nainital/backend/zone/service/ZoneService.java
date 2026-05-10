package com.nainital.backend.zone.service;

import com.nainital.backend.zone.dto.ZoneCheckResponse;
import com.nainital.backend.zone.model.DeliveryZone;
import com.nainital.backend.zone.repository.DeliveryZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final DeliveryZoneRepository zoneRepository;

    @Value("${order.delivery-fee:30}")
    private int defaultDeliveryFee;

    @Value("${order.delivery-fee-threshold:500}")
    private int defaultMinOrderForFree;

    @Value("${order.tax-rate:0.05}")
    private double defaultTaxRate;

    public ZoneCheckResponse checkZone(double lat, double lng) {
        List<DeliveryZone> zones = zoneRepository.findAllByActiveTrue();

        // No zones configured — global open mode
        if (zones.isEmpty()) {
            return ZoneCheckResponse.builder()
                    .inZone(true)
                    .zoneName("Nainital")
                    .etaLabel("6-10 mins")
                    .message("We deliver here!")
                    .deliveryFee(defaultDeliveryFee)
                    .minOrderForFree(defaultMinOrderForFree)
                    .taxRate(defaultTaxRate)
                    .build();
        }

        // Check "everywhere" zones first (global rate zones)
        Optional<DeliveryZone> everywhereZone = zones.stream()
                .filter(DeliveryZone::isEverywhere)
                .findFirst();

        if (everywhereZone.isPresent()) {
            DeliveryZone z = everywhereZone.get();
            return ZoneCheckResponse.builder()
                    .inZone(true)
                    .zoneName(z.getName())
                    .etaLabel(z.getEtaLabel() != null ? z.getEtaLabel() : "6-10 mins")
                    .message("We deliver here!")
                    .deliveryFee(z.getDeliveryFee())
                    .minOrderForFree(z.getMinOrderForFree())
                    .taxRate(effectiveTaxRate(z))
                    .build();
        }

        // Check polygon-based zones
        for (DeliveryZone zone : zones) {
            if (zone.getPolygon() != null && !zone.getPolygon().isEmpty()
                    && pointInPolygon(lat, lng, zone.getPolygon())) {
                return ZoneCheckResponse.builder()
                        .inZone(true)
                        .zoneName(zone.getName())
                        .etaLabel(zone.getEtaLabel() != null ? zone.getEtaLabel() : "6-10 mins")
                        .message("We deliver here!")
                        .deliveryFee(zone.getDeliveryFee())
                        .minOrderForFree(zone.getMinOrderForFree())
                        .taxRate(effectiveTaxRate(zone))
                        .build();
            }
        }

        return ZoneCheckResponse.builder()
                .inZone(false)
                .message("Sorry, we don't deliver to your location yet. We're expanding soon!")
                .deliveryFee(0)
                .minOrderForFree(0)
                .taxRate(defaultTaxRate)
                .build();
    }

    // Returns the delivery fee for a given lat/lng (used by CartService)
    public int getDeliveryFeeForLocation(double lat, double lng) {
        ZoneCheckResponse resp = checkZone(lat, lng);
        return resp.isInZone() ? resp.getDeliveryFee() : defaultDeliveryFee;
    }

    public int getDefaultDeliveryFee() { return defaultDeliveryFee; }
    public int getDefaultMinOrderForFree() { return defaultMinOrderForFree; }

    // Ray-casting algorithm: polygon is list of [lng, lat] pairs
    private boolean pointInPolygon(double lat, double lng, List<double[]> polygon) {
        if (polygon == null || polygon.size() < 3) return false;
        int n = polygon.size();
        boolean inside = false;
        int j = n - 1;
        for (int i = 0; i < n; i++) {
            double xi = polygon.get(i)[0]; // lng
            double yi = polygon.get(i)[1]; // lat
            double xj = polygon.get(j)[0];
            double yj = polygon.get(j)[1];
            boolean intersect = ((yi > lat) != (yj > lat))
                    && (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
            j = i;
        }
        return inside;
    }

    private double effectiveTaxRate(DeliveryZone zone) {
        return (zone.getTaxRate() > 0) ? zone.getTaxRate() : defaultTaxRate;
    }

    public DeliveryZone saveZone(DeliveryZone zone) {
        return zoneRepository.save(zone);
    }

    public DeliveryZone updateZone(String id, DeliveryZone zone) {
        zone.setId(id);
        return zoneRepository.save(zone);
    }

    public List<DeliveryZone> getAllZones() {
        return zoneRepository.findAll();
    }

    public void deleteZone(String id) {
        zoneRepository.deleteById(id);
    }
}
