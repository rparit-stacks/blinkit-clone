package com.nainital.backend.user.repository;

import com.nainital.backend.user.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {

    List<Address> findByUserId(String userId);

    Optional<Address> findByIdAndUserId(String id, String userId);

    List<Address> findByUserIdAndDefaultAddress(String userId, boolean defaultAddress);

    void deleteByIdAndUserId(String id, String userId);
}
