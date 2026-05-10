package com.nainital.backend.delivery.repository;

import com.nainital.backend.delivery.model.DeliveryAssignment;
import com.nainital.backend.delivery.model.DeliveryAssignmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository extends MongoRepository<DeliveryAssignment, String> {

    List<DeliveryAssignment> findAllByDeliveryPartnerIdOrderByCreatedAtDesc(String partnerId);

    List<DeliveryAssignment> findAllByDeliveryPartnerIdAndStatusOrderByCreatedAtDesc(
            String partnerId, DeliveryAssignmentStatus status);

    Optional<DeliveryAssignment> findBySubOrderId(String subOrderId);

    List<DeliveryAssignment> findAllByOrderByCreatedAtDesc();

    Optional<DeliveryAssignment> findByIdAndDeliveryPartnerId(String id, String partnerId);
}
