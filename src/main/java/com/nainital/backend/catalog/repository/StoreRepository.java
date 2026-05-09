package com.nainital.backend.catalog.repository;

import com.nainital.backend.catalog.model.Store;
import com.nainital.backend.catalog.model.StoreCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StoreRepository extends MongoRepository<Store, String> {
    List<Store> findAllByStoreCategoryAndActiveTrueOrderBySortOrderAsc(StoreCategory storeCategory);
    List<Store> findAllByActiveTrueOrderBySortOrderAsc();
}
