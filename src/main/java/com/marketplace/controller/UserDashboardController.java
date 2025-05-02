package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.model.UserDetails;
import com.marketplace.repository.UserRepository;
import com.marketplace.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(UserDashboardController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @GetMapping("/user/dashboard")
    public String showDashboard(Model model) {
        logger.info("Showing user dashboard");
        
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Fetch user and user details
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        UserDetails userDetails = userDetailsRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User details not found"));

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("userDetails", userDetails);

        return "user/dashboard";
    }
} 