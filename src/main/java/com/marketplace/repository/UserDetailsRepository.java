package com.marketplace.repository;

import com.marketplace.model.UserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends MongoRepository<UserDetails, String> {
    Optional<UserDetails> findByUserId(String userId);
} 