package com.nainital.backend.admin.controller;

import com.nainital.backend.admin.service.AdminCatalogService;
import com.nainital.backend.catalog.model.Product;
import com.nainital.backend.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminCatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String storeId) {
        List<Product> list;
        if (storeId != null) list = catalogService.getProductsByStore(storeId);
        else if (category != null) list = catalogService.getProductsByCategory(category);
        else list = catalogService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getProduct(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(@RequestBody Product product) {
        return ResponseEntity.ok(ApiResponse.ok("Created", catalogService.saveProduct(product)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(
            @PathVariable String id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(ApiResponse.ok("Updated", catalogService.saveProduct(product)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> patch(
            @PathVariable String id, @RequestBody Map<String, Object> fields) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", catalogService.updateProductFields(id, fields)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        catalogService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
