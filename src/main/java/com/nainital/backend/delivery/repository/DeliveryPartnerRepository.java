package com.nainital.backend.delivery.repository;

import com.nainital.backend.delivery.model.DeliveryPartner;
import com.nainital.backend.delivery.model.PartnerStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface DeliveryPartnerRepository extends MongoRepository<DeliveryPartner, String> {
    List<DeliveryPartner> findAllByStatus(PartnerStatus status);
    boolean existsByPhone(String phone);
    Optional<DeliveryPartner> findByPhone(String phone);
}
