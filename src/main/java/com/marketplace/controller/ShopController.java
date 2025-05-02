package com.marketplace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.marketplace.model.Post;
import com.marketplace.model.Shop;
import com.marketplace.repository.PostRepository;
import com.marketplace.repository.ShopRepository;
import com.marketplace.repository.UserRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/shop")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/posts")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public String shopPosts(Model model) {
        try {
            logger.info("=== Starting shopPosts method ===");
            
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.info("Current authenticated user: {}", username);
            logger.info("User roles: {}", auth.getAuthorities());
            
            // Add user role to model
            String userRole = auth.getAuthorities().stream()
                .findFirst()
                .map(grantedAuth -> grantedAuth.getAuthority())
                .orElse(null);
            model.addAttribute("userRole", userRole);
            
            // Get user details from database
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                logger.error("User not found in database: {}", username);
                model.addAttribute("error", "User not found");
                return "shop/posts";
            }
            
            String userId = user.get().getId();
            logger.info("Found user ID: {}", userId);
            
            // Get current shop owner's shop using user ID
            var shops = shopRepository.findByOwner(userId);
            logger.info("Found {} shops for user ID {}", shops.size(), userId);
            
            if (!shops.isEmpty()) {
                var shop = shops.get(0);
                logger.info("Found shop with ID: {} and owner: {}", shop.getId(), shop.getOwner());
                
                // Get all posts for this shop
                var posts = postRepository.findByShopId(shop.getId());
                logger.info("Found {} posts for shop {}", posts.size(), shop.getId());
                
                model.addAttribute("posts", posts);
                model.addAttribute("shop", shop);
            } else {
                logger.warn("No shop found for user ID: {}", userId);
                model.addAttribute("posts", new ArrayList<Post>());
            }
            
            logger.info("=== Completed shopPosts method ===");
            return "shop/posts";
        } catch (Exception e) {
            logger.error("Error in shopPosts: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load posts");
            return "shop/posts";
        }
    }

    @PostMapping("/posts/create")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public String createPost(@RequestParam String title, 
                           @RequestParam String description,
                           @RequestParam(required = false) MultipartFile image) {
        try {
            logger.info("=== Starting createPost method ===");
            logger.info("Received title: {}, description: {}", title, description);
            
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.info("Current authenticated user: {}", username);
            
            // Get user details from database
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                logger.error("User not found in database: {}", username);
                return "redirect:/shop/posts?error=user_not_found";
            }
            
            String userId = user.get().getId();
            
            // Get current shop owner's shop using user ID
            var shops = shopRepository.findByOwner(userId);
            if (!shops.isEmpty()) {
                var shop = shops.get(0);
                
                // Create new post
                Post post = new Post();
                post.setTitle(title);
                post.setDescription(description);
                post.setShopId(shop.getId());
                post.setStatus("ACTIVE");
                post.setCreatedAt(java.time.LocalDateTime.now());
                post.setUpdatedAt(java.time.LocalDateTime.now());
                
                // Handle image upload if present
                if (image != null && !image.isEmpty()) {
                    try {
                        // Create uploads directory structure
                        String uploadDir = "uploads/posts/" + shop.getId();
                        File dir = new File(uploadDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        
                        // Generate unique filename with timestamp
                        String originalFilename = image.getOriginalFilename();
                        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filename = timestamp + "_" + UUID.randomUUID().toString() + extension;
                        
                        // Save file
                        File file = new File(dir.getAbsolutePath() + File.separator + filename);
                        image.transferTo(file);
                        
                        // Set image path in post (relative to uploads directory)
                        post.setImageUrl("/uploads/posts/" + shop.getId() + "/" + filename);
                        logger.info("Image uploaded successfully: {}", post.getImageUrl());
                    } catch (Exception e) {
                        logger.error("Error uploading image: {}", e.getMessage(), e);
                        return "redirect:/shop/posts?error=image_upload_failed";
                    }
                }
                
                // Save post to MongoDB
                Post savedPost = postRepository.save(post);
                logger.info("Post saved successfully with ID: {}", savedPost.getId());
                
                return "redirect:/shop/posts";
            } else {
                logger.error("No shop found for user ID: {}", userId);
                return "redirect:/shop/posts?error=no_shop";
            }
        } catch (Exception e) {
            logger.error("Error in createPost: {}", e.getMessage(), e);
            return "redirect:/shop/posts?error=creation_failed";
        }
    }

    @PostMapping("/posts/{postId}/edit")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public String editPost(@PathVariable String postId, 
                         @RequestParam String title,
                         @RequestParam String description,
                         @RequestParam(required = false) MultipartFile image,
                         @RequestParam(required = false, defaultValue = "false") boolean removeImage) {
        try {
            logger.info("=== Starting editPost method ===");
            logger.info("Editing post with ID: {}", postId);
            
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get user details from database
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                logger.error("User not found in database: {}", username);
                return "redirect:/shop/posts?error=user_not_found";
            }
            
            String userId = user.get().getId();
            
            // Get current shop owner's shop
            var shops = shopRepository.findByOwner(userId);
            if (shops.isEmpty()) {
                logger.error("No shop found for user ID: {}", userId);
                return "redirect:/shop/posts?error=no_shop";
            }
            
            var shop = shops.get(0);
            
            // Find the post
            var postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                logger.error("Post not found with ID: {}", postId);
                return "redirect:/shop/posts?error=post_not_found";
            }
            
            var post = postOptional.get();
            
            // Verify the post belongs to the shop
            if (!post.getShopId().equals(shop.getId())) {
                logger.error("Post does not belong to shop. Post shop ID: {}, Current shop ID: {}", 
                           post.getShopId(), shop.getId());
                return "redirect:/shop/posts?error=unauthorized";
            }
            
            // Update post
            post.setTitle(title);
            post.setDescription(description);
            post.setUpdatedAt(java.time.LocalDateTime.now());
            
            // Handle image update
            if (removeImage) {
                // Remove existing image if it exists
                if (post.getImageUrl() != null) {
                    String imagePath = post.getImageUrl().replaceFirst("/uploads/", "uploads/");
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    post.setImageUrl(null);
                }
            } else if (image != null && !image.isEmpty()) {
                try {
                    // Remove existing image if it exists
                    if (post.getImageUrl() != null) {
                        String oldImagePath = post.getImageUrl().replaceFirst("/uploads/", "uploads/");
                        File oldImageFile = new File(oldImagePath);
                        if (oldImageFile.exists()) {
                            oldImageFile.delete();
                        }
                    }
                    
                    // Create uploads directory structure
                    String uploadDir = "uploads/posts/" + shop.getId();
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    
                    // Generate unique filename with timestamp
                    String originalFilename = image.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    String filename = timestamp + "_" + UUID.randomUUID().toString() + extension;
                    
                    // Save file
                    File file = new File(dir.getAbsolutePath() + File.separator + filename);
                    image.transferTo(file);
                    
                    // Set image path in post
                    post.setImageUrl("/uploads/posts/" + shop.getId() + "/" + filename);
                    logger.info("Image updated successfully: {}", post.getImageUrl());
                } catch (Exception e) {
                    logger.error("Error updating image: {}", e.getMessage(), e);
                    return "redirect:/shop/posts?error=image_upload_failed";
                }
            }
            
            postRepository.save(post);
            logger.info("Post updated successfully: {}", post);
            
            return "redirect:/shop/posts";
        } catch (Exception e) {
            logger.error("Error in editPost: {}", e.getMessage(), e);
            return "redirect:/shop/posts?error=edit_failed";
        }
    }

    @PostMapping("/posts/{postId}/delete")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public String deletePost(@PathVariable String postId) {
        try {
            logger.info("=== Starting deletePost method ===");
            logger.info("Deleting post with ID: {}", postId);
            
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get user details from database
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                logger.error("User not found in database: {}", username);
                return "redirect:/shop/posts?error=user_not_found";
            }
            
            String userId = user.get().getId();
            
            // Get current shop owner's shop
            var shops = shopRepository.findByOwner(userId);
            if (shops.isEmpty()) {
                logger.error("No shop found for user ID: {}", userId);
                return "redirect:/shop/posts?error=no_shop";
            }
            
            var shop = shops.get(0);
            
            // Find the post
            var postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                logger.error("Post not found with ID: {}", postId);
                return "redirect:/shop/posts?error=post_not_found";
            }
            
            var post = postOptional.get();
            
            // Verify the post belongs to the shop
            if (!post.getShopId().equals(shop.getId())) {
                logger.error("Post does not belong to shop. Post shop ID: {}, Current shop ID: {}", 
                           post.getShopId(), shop.getId());
                return "redirect:/shop/posts?error=unauthorized";
            }
            
            // Delete post
            postRepository.delete(post);
            logger.info("Post deleted successfully: {}", post);
            
            return "redirect:/shop/posts";
        } catch (Exception e) {
            logger.error("Error in deletePost: {}", e.getMessage(), e);
            return "redirect:/shop/posts?error=delete_failed";
        }
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public String settings(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            logger.info("Loading settings for user: {}", username);
            
            // Add user role to model
            String userRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuth -> grantedAuth.getAuthority())
                .orElse(null);
            model.addAttribute("userRole", userRole);
            
            // First get the user's ID
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                logger.error("User not found: {}", username);
                model.addAttribute("error", "User not found");
                return "shop/settings";
            }
            
            String userId = user.get().getId();
            logger.info("Found user ID: {} for username: {}", userId, username);
            
            // Get the current user's shop using user ID
            List<Shop> shops = shopRepository.findByOwner(userId);
            logger.info("Found {} shops for user ID: {}", shops.size(), userId);
            
            if (shops.isEmpty()) {
                logger.warn("No shop found for user ID: {}", userId);
                model.addAttribute("error", "No shop found. Please create a shop first.");
                Shop emptyShop = new Shop();
                model.addAttribute("shop", emptyShop);
            } else {
                Shop shop = shops.get(0);
                logger.info("Found shop - ID: {}, Name: {}, Owner: {}", 
                    shop.getId(), shop.getName(), shop.getOwner());
                model.addAttribute("shop", shop);
            }
            
            // Add categories and business types regardless of shop existence
            model.addAttribute("categories", Arrays.asList(
                "RETAIL", "FOOD", "SERVICE", "TECHNOLOGY", 
                "FASHION", "HEALTH", "EDUCATION", "OTHER"
            ));
            
            model.addAttribute("businessTypes", Arrays.asList(
                "SOLE_PROPRIETORSHIP", "PARTNERSHIP", 
                "CORPORATION", "LLC"
            ));
            
            return "shop/settings";
        } catch (Exception e) {
            logger.error("Error loading shop settings", e);
            model.addAttribute("error", "Error loading shop settings. Please try again later.");
            Shop emptyShop = new Shop();
            model.addAttribute("shop", emptyShop);
            return "shop/settings";
        }
    }

    @PostMapping("/settings/update")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public String updateSettings(@ModelAttribute Shop shop, 
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            logger.info("Updating settings for user: {}", username);
            
            // First get the user's ID
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                logger.error("User not found: {}", username);
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/shop/settings";
            }
            
            String userId = user.get().getId();
            logger.info("Found user ID: {} for username: {}", userId, username);
            
            // Get the existing shop using user ID
            List<Shop> existingShops = shopRepository.findByOwner(userId);
            logger.info("Found {} shops for user ID: {}", existingShops.size(), userId);
            
            if (existingShops.isEmpty()) {
                logger.warn("No shop found for user ID: {}", userId);
                redirectAttributes.addFlashAttribute("error", "No shop found. Please create a shop first.");
                return "redirect:/shop/settings";
            }
            
            Shop existingShop = existingShops.get(0);
            logger.info("Found existing shop - ID: {}, Name: {}, Owner: {}", 
                existingShop.getId(), existingShop.getName(), existingShop.getOwner());
            
            // Update all fields
            existingShop.setName(shop.getName());
            existingShop.setDescription(shop.getDescription());
            existingShop.setCategory(shop.getCategory());
            existingShop.setEmail(shop.getEmail());
            existingShop.setPhone(shop.getPhone());
            existingShop.setWebsite(shop.getWebsite());
            existingShop.setAddress(shop.getAddress());
            existingShop.setCity(shop.getCity());
            existingShop.setPostalCode(shop.getPostalCode());
            existingShop.setBusinessType(shop.getBusinessType());
            existingShop.setStatus(shop.getStatus());
            existingShop.setUpdatedAt(LocalDateTime.now());
            
            // Save the updated shop
            Shop savedShop = shopRepository.save(existingShop);
            logger.info("Successfully updated shop - ID: {}, Name: {}, Owner: {}", 
                savedShop.getId(), savedShop.getName(), savedShop.getOwner());
            
            redirectAttributes.addFlashAttribute("success", "Shop settings updated successfully");
            return "redirect:/shop/settings";
        } catch (Exception e) {
            logger.error("Error updating shop settings", e);
            redirectAttributes.addFlashAttribute("error", "Error updating shop settings. Please try again later.");
            return "redirect:/shop/settings";
        }
    }
} 