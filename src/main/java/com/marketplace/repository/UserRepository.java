package com.marketplace.repository;

import com.marketplace.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByStatus(String status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByRole(String role);
} 