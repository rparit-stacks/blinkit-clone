package com.nainital.backend.zone.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.zone.dto.ZoneCheckRequest;
import com.nainital.backend.zone.dto.ZoneCheckResponse;
import com.nainital.backend.zone.model.DeliveryZone;
import com.nainital.backend.zone.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    // ─── Public ──────────────────────────────────────────────────────────────

    @PostMapping("/api/zones/check")
    public ResponseEntity<ApiResponse<ZoneCheckResponse>> checkZone(
            @Valid @RequestBody ZoneCheckRequest req) {
        ZoneCheckResponse result = zoneService.checkZone(req.getLat(), req.getLng());
        return ResponseEntity.ok(ApiResponse.ok("Zone check complete", result));
    }

    // ─── Admin CRUD (under /api/admin/zones) ─────────────────────────────────

    @GetMapping("/api/admin/zones")
    public ResponseEntity<ApiResponse<List<DeliveryZone>>> getAllZones() {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.getAllZones()));
    }

    @PostMapping("/api/admin/zones")
    public ResponseEntity<ApiResponse<DeliveryZone>> createZone(
            @RequestBody DeliveryZone zone) {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.saveZone(zone)));
    }

    @PutMapping("/api/admin/zones/{id}")
    public ResponseEntity<ApiResponse<DeliveryZone>> updateZone(
            @PathVariable String id, @RequestBody DeliveryZone zone) {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.updateZone(id, zone)));
    }

    @PatchMapping("/api/admin/zones/{id}/toggle")
    public ResponseEntity<ApiResponse<DeliveryZone>> toggleZone(@PathVariable String id) {
        List<DeliveryZone> all = zoneService.getAllZones();
        DeliveryZone zone = all.stream().filter(z -> z.getId().equals(id))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Zone not found"));
        zone.setActive(!zone.isActive());
        return ResponseEntity.ok(ApiResponse.ok(zoneService.saveZone(zone)));
    }

    @DeleteMapping("/api/admin/zones/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable String id) {
        zoneService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
