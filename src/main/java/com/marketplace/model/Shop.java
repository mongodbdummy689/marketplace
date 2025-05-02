package com.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "shops")
public class Shop {
    @Id
    private String id;

    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Owner username is required")
    @Field("owner")  // Explicitly map to MongoDB field
    private String owner;

    @Field("registration_date")
    private LocalDateTime registrationDate;

    private String status;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phone;

    @Field("business_type")
    private String businessType;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "City is required")
    private String city;

    @Field("postal_code")
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Please provide a valid postal code")
    private String postalCode;

    @NotBlank(message = "Address is required")
    private String address;

    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", 
             message = "Please provide a valid website URL")
    private String website;

    @NotBlank(message = "Category is required")
    private String category;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("profile_photo")
    private String profilePhoto;

    public Shop() {
        this.registrationDate = LocalDateTime.now();
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Shop(String name, String location, String owner) {
        this();
        this.name = name;
        this.location = location;
        this.owner = owner;
    }

    // Custom setter for registrationDate
    public void setRegistrationDate(LocalDateTime date) {
        this.registrationDate = date != null ? date : LocalDateTime.now();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", owner='" + owner + '\'' +
                ", registrationDate=" + registrationDate +
                ", status='" + status + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", businessType='" + businessType + '\'' +
                ", description='" + description + '\'' +
                ", city='" + city + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", address='" + address + '\'' +
                ", website='" + website + '\'' +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", profilePhoto='" + profilePhoto + '\'' +
                '}';
    }
} 