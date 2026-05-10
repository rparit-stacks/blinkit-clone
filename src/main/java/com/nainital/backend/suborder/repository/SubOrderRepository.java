package com.nainital.backend.suborder.repository;

import com.nainital.backend.suborder.model.SubOrder;
import com.nainital.backend.order.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubOrderRepository extends MongoRepository<SubOrder, String> {
    List<SubOrder> findAllByMasterOrderIdOrderByCreatedAtDesc(String masterOrderId);
    List<SubOrder> findAllBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<SubOrder> findAllByStoreIdOrderByCreatedAtDesc(String storeId);
    List<SubOrder> findAllByCustomerIdOrderByCreatedAtDesc(String customerId);
    Optional<SubOrder> findByIdAndSellerId(String id, String sellerId);
    long countBySellerIdAndStatus(String sellerId, OrderStatus status);
    long countBySellerId(String sellerId);
    List<SubOrder> findAllBySellerIdAndEarningCreditedFalseAndStatus(String sellerId, OrderStatus status);
}
