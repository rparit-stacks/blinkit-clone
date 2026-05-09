package com.nainital.backend.delivery.controller;

import com.nainital.backend.common.ApiResponse;
import com.nainital.backend.delivery.dto.DeliveryPartnerRequest;
import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.service.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryPartner>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(service.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryPartner>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryPartner>> create(
            @Valid @RequestBody DeliveryPartnerRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Created", service.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryPartner>> update(
            @PathVariable String id, @Valid @RequestBody DeliveryPartnerRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", service.update(id, req)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<DeliveryPartner>> approve(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(service.approve(id)));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<ApiResponse<DeliveryPartner>> block(
            @PathVariable String id, @RequestParam boolean block) {
        return ResponseEntity.ok(ApiResponse.ok(service.block(id, block)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
