package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class User {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SHOP_OWNER = "SHOP_OWNER";
    public static final String ROLE_USER = "USER";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_PENDING = "PENDING";

    @Id
    private String id;
    private String username;
    private String email;
    private String role;
    private String status;
    private LocalDateTime registrationDate;
    private String password;

    public User() {
        this.registrationDate = LocalDateTime.now();
        this.status = STATUS_ACTIVE;
    }

    public void setRole(String role) {
        if (!ROLE_ADMIN.equals(role) && !ROLE_SHOP_OWNER.equals(role) && !ROLE_USER.equals(role)) {
            throw new IllegalArgumentException("Invalid role. Must be either ADMIN, SHOP_OWNER, or USER");
        }
        this.role = role;
    }

    public void setStatus(String status) {
        if (!STATUS_ACTIVE.equals(status) && !STATUS_INACTIVE.equals(status) && !STATUS_PENDING.equals(status)) {
            throw new IllegalArgumentException("Invalid status. Must be either ACTIVE, INACTIVE, or PENDING");
        }
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
} 