package com.marketplace.repository;

import com.marketplace.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByStatus(String status);
    List<Post> findByShopId(String shopId);
    List<Post> findByCategory(String category);
    List<Post> findByStatusOrderByCreatedAtDesc(String status);
} 