package com.marketplace.config;

import com.marketplace.model.Shop;
import com.marketplace.model.User;
import com.marketplace.model.Post;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.marketplace.repository")
public class MongoConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        try {
            logger.info("Initializing MongoDB indexes...");
            
            // Test MongoDB connection
            logger.info("Testing MongoDB connection...");
            mongoTemplate.getDb().listCollectionNames().first();
            logger.info("MongoDB connection successful!");

            // Create indexes for User collection
            logger.info("Creating indexes for User collection...");
            mongoTemplate.indexOps(User.class)
                .ensureIndex(new Index().on("username", org.springframework.data.domain.Sort.Direction.ASC).unique());
            mongoTemplate.indexOps(User.class)
                .ensureIndex(new Index().on("email", org.springframework.data.domain.Sort.Direction.ASC).unique());
            mongoTemplate.indexOps(User.class)
                .ensureIndex(new Index().on("role", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(User.class)
                .ensureIndex(new Index().on("status", org.springframework.data.domain.Sort.Direction.ASC));

            // Create indexes for Shop collection
            logger.info("Creating indexes for Shop collection...");
            mongoTemplate.indexOps(Shop.class)
                .ensureIndex(new Index().on("name", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(Shop.class)
                .ensureIndex(new Index().on("owner", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(Shop.class)
                .ensureIndex(new Index().on("status", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(Shop.class)
                .ensureIndex(new Index().on("businessType", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(Shop.class)
                .ensureIndex(new Index().on("city", org.springframework.data.domain.Sort.Direction.ASC));

            // Create indexes for Post collection
            logger.info("Creating indexes for Post collection...");
            mongoTemplate.indexOps(Post.class)
                .ensureIndex(new Index().on("owner", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(Post.class)
                .ensureIndex(new Index().on("status", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(Post.class)
                .ensureIndex(new Index().on("category", org.springframework.data.domain.Sort.Direction.ASC));
            mongoTemplate.indexOps(Post.class)
                .ensureIndex(new Index().on("creationDate", org.springframework.data.domain.Sort.Direction.DESC));

            logger.info("MongoDB indexes initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing MongoDB: {}", e.getMessage(), e);
            throw e;
        }
    }
} 