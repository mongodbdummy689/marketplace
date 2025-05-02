package com.marketplace.repository;

import com.marketplace.model.Shop;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends MongoRepository<Shop, String> {
    List<Shop> findByStatus(String status);
    List<Shop> findByOwner(String owner);
    List<Shop> findByBusinessType(String businessType);
    List<Shop> findByCity(String city);
} 