package com.marketplace.config;

import com.marketplace.model.User;
import com.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DefaultAdminConfig {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdminConfig.class);
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@marketplace.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createDefaultAdmin() {
        return args -> {
            // Check if any admin user exists
            if (!userRepository.existsByRole(User.ROLE_ADMIN)) {
                logger.info("No admin user found. Creating default admin user...");
                
                User admin = new User();
                admin.setUsername(DEFAULT_ADMIN_USERNAME);
                admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                admin.setEmail(DEFAULT_ADMIN_EMAIL);
                admin.setRole(User.ROLE_ADMIN);
                admin.setStatus(User.STATUS_ACTIVE);
                
                userRepository.save(admin);
                logger.info("Default admin user created successfully with username: {}", DEFAULT_ADMIN_USERNAME);
            } else {
                logger.info("Admin user(s) already exist in the database");
            }
        };
    }
} 