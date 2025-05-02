package com.marketplace.repository;

import com.marketplace.entity.DonationPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonationPostRepository extends MongoRepository<DonationPost, String> {
    List<DonationPost> findByStatus(String status);
    List<DonationPost> findByEndDateBeforeAndStatus(LocalDate date, String status);
    List<DonationPost> findByType(String type);
} 