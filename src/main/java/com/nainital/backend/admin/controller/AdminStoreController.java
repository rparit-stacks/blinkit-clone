package com.nainital.backend.admin.controller;

import com.nainital.backend.admin.service.AdminCatalogService;
import com.nainital.backend.catalog.model.Store;
import com.nainital.backend.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {

    private final AdminCatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Store>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getAllStores()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getStore(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Store>> create(@RequestBody Store store) {
        return ResponseEntity.ok(ApiResponse.ok("Created", catalogService.saveStore(store)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> update(
            @PathVariable String id, @RequestBody Store store) {
        store.setId(id);
        return ResponseEntity.ok(ApiResponse.ok("Updated", catalogService.saveStore(store)));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Store>> toggle(
            @PathVariable String id, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.toggleStore(id, active)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        catalogService.deleteStore(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
