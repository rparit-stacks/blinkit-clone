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
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    // Public endpoint — no auth required
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<ZoneCheckResponse>> checkZone(
            @Valid @RequestBody ZoneCheckRequest req) {
        ZoneCheckResponse result = zoneService.checkZone(req.getLat(), req.getLng());
        return ResponseEntity.ok(ApiResponse.ok("Zone check complete", result));
    }

    // Admin endpoints (will be secured via admin role later)
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryZone>>> getAllZones() {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.getAllZones()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryZone>> createZone(
            @RequestBody DeliveryZone zone) {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.saveZone(zone)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable String id) {
        zoneService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
