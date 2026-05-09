package com.nainital.backend.admin.service;

import com.nainital.backend.catalog.model.Product;
import com.nainital.backend.catalog.model.Store;
import com.nainital.backend.catalog.model.StoreCategory;
import com.nainital.backend.catalog.repository.ProductRepository;
import com.nainital.backend.catalog.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final ProductRepository productRepo;
    private final StoreRepository storeRepo;

    // ─── Products ──────────────────────────────────────────────────────────────

    public List<Product> getAllProducts() { return productRepo.findAll(); }

    public Product getProduct(String id) {
        return productRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product saveProduct(Product product) {
        if (product.getId() != null && !product.getId().isBlank()) {
            productRepo.findById(product.getId()).ifPresent(existing -> {
                if (product.getApproved() == null) {
                    product.setApproved(existing.getApproved());
                }
            });
        } else if (product.getApproved() == null) {
            product.setApproved(Boolean.FALSE);
        }
        return productRepo.save(product);
    }

    public Product updateProductFields(String id, Map<String, Object> fields) {
        Product p = getProduct(id);
        fields.forEach((key, val) -> {
            switch (key) {
                case "name"        -> p.setName((String) val);
                case "description" -> p.setDescription((String) val);
                case "price"       -> p.setPrice(((Number) val).intValue());
                case "originalPrice" -> p.setOriginalPrice(((Number) val).intValue());
                case "unit"        -> p.setUnit((String) val);
                case "badge"       -> p.setBadge((String) val);
                case "available"   -> p.setAvailable((Boolean) val);
                case "approved"    -> p.setApproved((Boolean) val);
                case "categorySlug"-> p.setCategorySlug((String) val);
                case "image"       -> p.setImage((String) val);
                case "sortOrder"   -> p.setSortOrder(((Number) val).intValue());
            }
        });
        return productRepo.save(p);
    }

    public void deleteProduct(String id) {
        if (!productRepo.existsById(id)) throw new IllegalArgumentException("Product not found");
        productRepo.deleteById(id);
    }

    // ─── Stores ────────────────────────────────────────────────────────────────

    public List<Store> getAllStores() { return storeRepo.findAll(); }

    public Store getStore(String id) {
        return storeRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }

    public Store saveStore(Store store) { return storeRepo.save(store); }

    public Store toggleStore(String id, boolean active) {
        Store s = getStore(id);
        s.setActive(active);
        return storeRepo.save(s);
    }

    public void deleteStore(String id) {
        if (!storeRepo.existsById(id)) throw new IllegalArgumentException("Store not found");
        storeRepo.deleteById(id);
    }

    public List<Product> getProductsByStore(String storeId) {
        return productRepo.findAll().stream()
                .filter(p -> storeId.equals(p.getStoreId())).toList();
    }

    public List<Product> getProductsByCategory(String categoryStr) {
        try {
            StoreCategory cat = StoreCategory.valueOf(categoryStr.toUpperCase());
            return productRepo.findAll().stream()
                    .filter(p -> cat == p.getStoreCategory()).toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown category: " + categoryStr);
        }
    }
}
