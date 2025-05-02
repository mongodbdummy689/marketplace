package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.model.UserDetails;
import com.marketplace.repository.UserRepository;
import com.marketplace.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class UserRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register/user")
    @Transactional
    public String registerUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        logger.info("Starting user registration process for: {}", username);
        logger.debug("Received form data - Username: {}, Email: {}, First Name: {}", username, email, firstName);

        try {
            // Validate input data
            logger.info("Validating input data...");
            if (firstName == null || firstName.trim().isEmpty()) {
                logger.warn("Validation failed: First name is empty");
                throw new IllegalArgumentException("First name is required");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                logger.warn("Validation failed: Last name is empty");
                throw new IllegalArgumentException("Last name is required");
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
                return "login";
            }

            // Validate password strength
            if (password.length() < 8) {
                logger.warn("Password validation failed: Password too short for user: {}", username);
                model.addAttribute("error", "Password must be at least 8 characters long");
                return "login";
            }

            // Check if username already exists
            logger.info("Checking for existing username: {}", username);
            if (userRepository.findByUsername(username).isPresent()) {
                logger.warn("Username already exists: {}", username);
                model.addAttribute("error", "Username already exists");
                return "login";
            }

            // Check if email already exists
            logger.info("Checking for existing email: {}", email);
            if (userRepository.findByEmail(email).isPresent()) {
                logger.warn("Email already registered: {}", email);
                model.addAttribute("error", "Email already registered");
                return "login";
            }

            // Create and save user (login information)
            logger.info("Creating new user...");
            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(User.ROLE_USER);
            user.setStatus(User.STATUS_ACTIVE);
            
            logger.debug("User object before save: {}", user);
            user = userRepository.save(user);
            logger.info("User created successfully with ID: {}", user.getId());

            // Create and save user details
            logger.info("Creating user details...");
            UserDetails userDetails = new UserDetails();
            userDetails.setUserId(user.getId());
            userDetails.setFirstName(firstName.trim());
            userDetails.setLastName(lastName.trim());
            userDetails.setPhone(phone.trim());
            userDetails.setAddress(address.trim());
            userDetails.setCity(city.trim());
            userDetails.setPostalCode(postalCode.trim());
            
            logger.debug("UserDetails object before save: {}", userDetails);
            userDetails = userDetailsRepository.save(userDetails);
            logger.info("User details created successfully with ID: {}", userDetails.getId());

            logger.info("Registration completed successfully for user: {}", username);
            return "redirect:/login?userRegistered=true";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "login";
        } catch (Exception e) {
            logger.error("Error during registration: {}", e.getMessage(), e);
            logger.error("Stack trace: ", e);
            model.addAttribute("error", "Error during registration: " + e.getMessage());
            return "login";
        }
    }
} 