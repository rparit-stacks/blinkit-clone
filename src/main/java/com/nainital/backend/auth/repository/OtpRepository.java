package com.nainital.backend.auth.repository;

import com.nainital.backend.user.model.OtpRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends MongoRepository<OtpRecord, String> {

    Optional<OtpRecord> findTopByEmailOrderByExpiresAtDesc(String email);

    void deleteByEmail(String email);
}
