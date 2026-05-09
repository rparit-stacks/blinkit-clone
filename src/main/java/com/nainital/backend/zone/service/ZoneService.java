package com.nainital.backend.zone.service;

import com.nainital.backend.zone.dto.ZoneCheckResponse;
import com.nainital.backend.zone.model.DeliveryZone;
import com.nainital.backend.zone.repository.DeliveryZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final DeliveryZoneRepository zoneRepository;

    public ZoneCheckResponse checkZone(double lat, double lng) {
        List<DeliveryZone> zones = zoneRepository.findAllByActiveTrue();

        // If no zones configured yet, treat everything as in-zone (launch mode)
        if (zones.isEmpty()) {
            return new ZoneCheckResponse(true, "Nainital", "6-10 mins",
                    "We deliver here!");
        }

        for (DeliveryZone zone : zones) {
            if (pointInPolygon(lat, lng, zone.getPolygon())) {
                return new ZoneCheckResponse(true, zone.getName(),
                        zone.getEtaLabel() != null ? zone.getEtaLabel() : "6-10 mins",
                        "We deliver here!");
            }
        }

        return new ZoneCheckResponse(false, null, null,
                "Sorry, we don't deliver to this location yet.");
    }

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

    public DeliveryZone saveZone(DeliveryZone zone) {
        return zoneRepository.save(zone);
    }

    public List<DeliveryZone> getAllZones() {
        return zoneRepository.findAll();
    }

    public void deleteZone(String id) {
        zoneRepository.deleteById(id);
    }
}
