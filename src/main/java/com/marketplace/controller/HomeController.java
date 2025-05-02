package com.marketplace.controller;

import com.marketplace.dto.DonationPostResponse;
import com.marketplace.model.Post;
import com.marketplace.repository.PostRepository;
import com.marketplace.repository.ShopRepository;
import com.marketplace.service.DonationPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private DonationPostService donationPostService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // Get user's role from authentication
        String userRole = authentication.getAuthorities().stream()
            .findFirst()
            .map(auth -> auth.getAuthority())
            .orElse(null);

        // Fetch latest active posts
        List<Post> latestPosts = postRepository.findByStatusOrderByCreatedAtDesc("ACTIVE")
            .stream()
            .limit(12) // Limit to 12 latest posts
            .collect(Collectors.toList());

        // Add shop information to posts
        latestPosts.forEach(post -> {
            if (post.getShopId() != null) {
                shopRepository.findById(post.getShopId())
                    .ifPresent(shop -> {
                        post.setShop(shop);
                        post.setShopName(shop.getName());
                    });
            }
        });

        // Fetch donation posts
        List<DonationPostResponse> donationPosts = donationPostService.getAllDonationPosts();

        model.addAttribute("posts", latestPosts);
        model.addAttribute("donationPosts", donationPosts);
        model.addAttribute("userRole", userRole);
        return "home";
    }
} 