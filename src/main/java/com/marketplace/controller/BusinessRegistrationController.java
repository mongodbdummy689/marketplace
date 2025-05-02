package com.marketplace.controller;

import com.marketplace.model.Shop;
import com.marketplace.model.User;
import com.marketplace.repository.ShopRepository;
import com.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.UUID;

@Controller
public class BusinessRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessRegistrationController.class);

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register/business")
    public String showRegistrationForm() {
        logger.info("Showing business registration form");
        return "register-business";
    }

    @PostMapping("/register/business")
    @Transactional
    public String registerBusiness(
            @RequestParam String businessName,
            @RequestParam String businessType,
            @RequestParam String businessDescription,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) MultipartFile profilePhoto,
            Model model) {

        logger.info("Starting business registration process for: {}", businessName);
        logger.debug("Received form data - Username: {}, Email: {}, Business Type: {}", username, email, businessType);

        try {
            // Log MongoDB connection status
            logger.info("Checking MongoDB connection...");
            logger.info("UserRepository is initialized: {}", userRepository != null);
            logger.info("ShopRepository is initialized: {}", shopRepository != null);

            // Validate input data
            logger.info("Validating input data...");
            if (businessName == null || businessName.trim().isEmpty()) {
                logger.warn("Validation failed: Business name is empty");
                throw new IllegalArgumentException("Business name is required");
            }
            if (businessType == null || businessType.trim().isEmpty()) {
                logger.warn("Validation failed: Business type is empty");
                throw new IllegalArgumentException("Business type is required");
            }
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Validation failed: Email is empty");
                throw new IllegalArgumentException("Email is required");
            }
            if (phone == null || phone.trim().isEmpty()) {
                logger.warn("Validation failed: Phone number is empty");
                throw new IllegalArgumentException("Phone number is required");
            }
            if (address == null || address.trim().isEmpty()) {
                logger.warn("Validation failed: Address is empty");
                throw new IllegalArgumentException("Address is required");
            }
            if (city == null || city.trim().isEmpty()) {
                logger.warn("Validation failed: City is empty");
                throw new IllegalArgumentException("City is required");
            }
            if (postalCode == null || postalCode.trim().isEmpty()) {
                logger.warn("Validation failed: Postal code is empty");
                throw new IllegalArgumentException("Postal code is required");
            }

            // Validate password match
            logger.info("Validating password...");
            if (!password.equals(confirmPassword)) {
                logger.warn("Password validation failed: Passwords do not match for user: {}", username);
                model.addAttribute("error", "Passwords do not match");
                return "register-business";
            }

            // Validate password strength
            if (password.length() < 8) {
                logger.warn("Password validation failed: Password too short for user: {}", username);
                model.addAttribute("error", "Password must be at least 8 characters long");
                return "register-business";
            }

            // Check if username already exists
            logger.info("Checking for existing username: {}", username);
            if (userRepository.findByUsername(username).isPresent()) {
                logger.warn("Username already exists: {}", username);
                model.addAttribute("error", "Username already exists");
                return "register-business";
            }

            // Check if email already exists
            logger.info("Checking for existing email: {}", email);
            if (userRepository.findByEmail(email).isPresent()) {
                logger.warn("Email already registered: {}", email);
                model.addAttribute("error", "Email already registered");
                return "register-business";
            }

            // Create and save user
            logger.info("Creating new user...");
            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("SHOP_OWNER");
            user.setStatus("PENDING");
            
            logger.debug("User object before save: {}", user);
            user = userRepository.save(user);
            logger.info("User created successfully with ID: {}", user.getId());

            // Create and save shop
            logger.info("Creating new shop...");
            Shop shop = new Shop();
            shop.setName(businessName.trim());
            shop.setBusinessType(businessType.trim());
            shop.setDescription(businessDescription.trim());
            shop.setEmail(email.trim());
            shop.setPhone(phone.trim());
            shop.setLocation(address.trim());
            shop.setCity(city.trim());
            shop.setPostalCode(postalCode.trim());
            shop.setOwner(user.getId());
            shop.setStatus("PENDING");

            // Handle profile photo upload
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                try {
                    // Create uploads directory structure
                    String uploadDir = "uploads/shops/" + user.getId();
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    
                    // Generate unique filename with timestamp
                    String originalFilename = profilePhoto.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String filename = timestamp + "_" + UUID.randomUUID().toString() + extension;
                    
                    // Save file
                    File file = new File(dir.getAbsolutePath() + File.separator + filename);
                    profilePhoto.transferTo(file);
                    
                    // Set profile photo path in shop (relative to uploads directory)
                    shop.setProfilePhoto("/uploads/shops/" + user.getId() + "/" + filename);
                    logger.info("Profile photo uploaded successfully: {}", shop.getProfilePhoto());
                } catch (Exception e) {
                    logger.error("Error uploading profile photo: {}", e.getMessage(), e);
                    model.addAttribute("error", "Error uploading profile photo. Please try again.");
                    return "register-business";
                }
            } else {
                // Set default profile photo
                shop.setProfilePhoto("/images/default-profile.png");
            }
            
            logger.debug("Shop object before save: {}", shop);
            shop = shopRepository.save(shop);
            logger.info("Shop created successfully with ID: {}", shop.getId());

            logger.info("Registration completed successfully for business: {}", businessName);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register-business";
        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);
            logger.error("Stack trace: ", e);
            model.addAttribute("error", "Error during registration: " + e.getMessage());
            return "register-business";
        }
    }
} 