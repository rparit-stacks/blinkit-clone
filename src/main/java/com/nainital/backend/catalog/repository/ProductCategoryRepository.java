package com.nainital.backend.catalog.repository;

import com.nainital.backend.catalog.model.ProductCategory;
import com.nainital.backend.catalog.model.StoreCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductCategoryRepository extends MongoRepository<ProductCategory, String> {
    List<ProductCategory> findAllByStoreCategoryAndActiveTrueOrderBySortOrderAsc(StoreCategory storeCategory);
}
