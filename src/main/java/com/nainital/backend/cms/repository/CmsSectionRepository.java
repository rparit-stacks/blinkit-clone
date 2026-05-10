package com.nainital.backend.cms.repository;

import com.nainital.backend.cms.model.CmsSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface CmsSectionRepository extends MongoRepository<CmsSection, String> {
    List<CmsSection> findAllByActiveTrueOrderBySortOrderAsc();

    @Query("{ 'active': true, $or: [{'storeCategory': null}, {'storeCategory': ?0}] }")
    List<CmsSection> findActiveByStoreCategory(String storeCategory);
}
