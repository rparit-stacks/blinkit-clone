package com.nainital.backend.catalog.repository;

import com.nainital.backend.catalog.model.Product;
import com.nainital.backend.catalog.model.StoreCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findAllByStoreCategoryAndAvailableTrueOrderBySortOrderAsc(StoreCategory storeCategory);

    List<Product> findAllByStoreCategoryAndCategorySlugAndAvailableTrueOrderBySortOrderAsc(
            StoreCategory storeCategory, String categorySlug);

    List<Product> findAllByStoreIdAndAvailableTrueOrderBySortOrderAsc(String storeId);

    List<Product> findAllByRestaurantIdAndAvailableTrueOrderBySortOrderAsc(String restaurantId);

    List<Product> findAllByStoreCategoryAndNameContainingIgnoreCaseAndAvailableTrue(
            StoreCategory storeCategory, String name);
}
