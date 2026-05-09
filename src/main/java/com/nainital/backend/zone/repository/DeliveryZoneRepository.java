package com.nainital.backend.zone.repository;

import com.nainital.backend.zone.model.DeliveryZone;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DeliveryZoneRepository extends MongoRepository<DeliveryZone, String> {
    List<DeliveryZone> findAllByActiveTrue();
}
