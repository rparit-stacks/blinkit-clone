package com.nainital.backend.catalog.repository;

import com.nainital.backend.catalog.model.Banner;
import com.nainital.backend.catalog.model.StoreCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BannerRepository extends MongoRepository<Banner, String> {
    List<Banner> findAllByStoreCategoryAndActiveTrueOrderBySortOrderAsc(StoreCategory storeCategory);
}
