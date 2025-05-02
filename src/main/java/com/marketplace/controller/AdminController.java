package com.marketplace.controller;

import com.marketplace.model.Activity;
import com.marketplace.model.Shop;
import com.marketplace.model.Post;
import com.marketplace.model.User;
import com.marketplace.repository.ShopRepository;
import com.marketplace.repository.PostRepository;
import com.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.marketplace.dto.DonationPostResponse;
import com.marketplace.service.DonationPostService;
import com.marketplace.dto.DonationPostRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationPostService donationPostService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model, HttpServletRequest request) {
        return "redirect:/admin/shops";
    }

    @GetMapping("/shops")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminShops(Model model, HttpServletRequest request) {
        try {
            logger.info("Admin accessing shops management page...");
            
            // Add current path to model
            model.addAttribute("currentPath", request.getRequestURI());
            
            // Add debug mode if requested
            boolean debugMode = "true".equalsIgnoreCase(request.getParameter("debug"));
            model.addAttribute("debugMode", debugMode);
            
            // Get shops from MongoDB
            logger.info("Retrieving pending shops from database...");
            List<Shop> pendingShops = shopRepository.findByStatus("PENDING");
            logger.info("Found {} pending shops", pendingShops != null ? pendingShops.size() : 0);
            if (pendingShops != null) {
                pendingShops.forEach(shop -> logger.debug("Pending shop: ID={}, Name={}, Status={}, Owner={}", 
                    shop.getId(), shop.getName(), shop.getStatus(), shop.getOwner()));
            }
            
            logger.info("Retrieving active shops from database...");
            List<Shop> activeShops = shopRepository.findByStatus("ACTIVE");
            logger.info("Found {} active shops", activeShops != null ? activeShops.size() : 0);
            if (activeShops != null) {
                activeShops.forEach(shop -> logger.debug("Active shop: ID={}, Name={}, Status={}, Owner={}", 
                    shop.getId(), shop.getName(), shop.getStatus(), shop.getOwner()));
            }
            
            logger.info("Retrieving inactive shops from database...");
            List<Shop> inactiveShops = shopRepository.findByStatus("INACTIVE");
            logger.info("Found {} inactive shops", inactiveShops != null ? inactiveShops.size() : 0);
            if (inactiveShops != null) {
                inactiveShops.forEach(shop -> logger.debug("Inactive shop: ID={}, Name={}, Status={}, Owner={}", 
                    shop.getId(), shop.getName(), shop.getStatus(), shop.getOwner()));
            }
            
            // Create a combined list of all shops
            List<Shop> allShops = new ArrayList<>();
            if (activeShops != null) allShops.addAll(activeShops);
            if (pendingShops != null) allShops.addAll(pendingShops);
            if (inactiveShops != null) allShops.addAll(inactiveShops);
            
            logger.info("Total shops retrieved: {}", allShops.size());
            
            // Get shop owner details for each shop
            logger.info("Retrieving shop owner details...");
            Map<String, User> shopOwners = new HashMap<>();
            for (Shop shop : allShops) {
                if (shop.getOwner() != null) {
                    logger.debug("Retrieving owner details for shop ID: {}", shop.getId());
                    userRepository.findById(shop.getOwner())
                        .ifPresent(user -> {
                            shopOwners.put(shop.getId(), user);
                            logger.debug("Owner details retrieved for shop ID: {}, Owner: {}, Email: {}", 
                                shop.getId(), user.getUsername(), user.getEmail());
                        });
                } else {
                    logger.warn("Shop {} has no owner ID", shop.getId());
                }
            }
            logger.info("Retrieved owner details for {} shops", shopOwners.size());
            
            // Add shops and owners to model
            model.addAttribute("pendingShops", pendingShops != null ? pendingShops : new ArrayList<>());
            model.addAttribute("activeShops", activeShops != null ? activeShops : new ArrayList<>());
            model.addAttribute("inactiveShops", inactiveShops != null ? inactiveShops : new ArrayList<>());
            model.addAttribute("allShops", allShops);
            model.addAttribute("totalShops", allShops.size());
            model.addAttribute("shopOwners", shopOwners);
            
            // Log model attributes for debugging
            logger.debug("Model attributes:");
            logger.debug("- pendingShops size: {}", ((List<?>)model.getAttribute("pendingShops")).size());
            logger.debug("- activeShops size: {}", ((List<?>)model.getAttribute("activeShops")).size());
            logger.debug("- inactiveShops size: {}", ((List<?>)model.getAttribute("inactiveShops")).size());
            logger.debug("- allShops size: {}", ((List<?>)model.getAttribute("allShops")).size());
            logger.debug("- shopOwners size: {}", ((Map<?, ?>)model.getAttribute("shopOwners")).size());
            
            logger.info("Admin shops page loaded successfully with {} total shops and {} shop owners", 
                allShops.size(), shopOwners.size());
            return "admin/shops";
        } catch (Exception e) {
            logger.error("Error loading admin shops page: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/shops/{shopId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String approveShop(@PathVariable String shopId) {
        try {
            logger.info("Approving shop with ID: {}", shopId);
            
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            // Update shop status
            shop.setStatus("ACTIVE");
            shopRepository.save(shop);
            
            // Update owner status if exists
            if (shop.getOwner() != null) {
                userRepository.findById(shop.getOwner())
                    .ifPresent(user -> {
                        user.setStatus("ACTIVE");
                        userRepository.save(user);
                    });
            }
            
            logger.info("Shop approved successfully");
            return "Shop approved successfully";
        } catch (Exception e) {
            logger.error("Error approving shop: {}", e.getMessage(), e);
            return "Error approving shop: " + e.getMessage();
        }
    }

    @PostMapping("/shops/{shopId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String rejectShop(@PathVariable String shopId) {
        try {
            logger.info("Rejecting shop with ID: {}", shopId);
            
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            // Update shop status
            shop.setStatus("REJECTED");
            shopRepository.save(shop);
            
            // Update owner status if exists
            if (shop.getOwner() != null) {
                userRepository.findById(shop.getOwner())
                    .ifPresent(user -> {
                        user.setStatus("INACTIVE");
                        userRepository.save(user);
                    });
            }
            
            logger.info("Shop rejected successfully");
            return "Shop rejected successfully";
        } catch (Exception e) {
            logger.error("Error rejecting shop: {}", e.getMessage(), e);
            return "Error rejecting shop: " + e.getMessage();
        }
    }

    @PostMapping("/shops/{shopId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String activateShop(@PathVariable String shopId) {
        try {
            logger.info("Activating shop with ID: {}", shopId);
            
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            shop.setStatus("ACTIVE");
            shopRepository.save(shop);
            
            logger.info("Shop activated successfully");
            return "Shop activated successfully";
        } catch (Exception e) {
            logger.error("Error activating shop: {}", e.getMessage(), e);
            return "Error activating shop: " + e.getMessage();
        }
    }

    @PostMapping("/shops/{shopId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String deactivateShop(@PathVariable String shopId) {
        try {
            logger.info("Deactivating shop with ID: {}", shopId);
            
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            shop.setStatus("INACTIVE");
            shopRepository.save(shop);
            
            logger.info("Shop deactivated successfully");
            return "Shop deactivated successfully";
        } catch (Exception e) {
            logger.error("Error deactivating shop: {}", e.getMessage(), e);
            return "Error deactivating shop: " + e.getMessage();
        }
    }

    @DeleteMapping("/shops/{shopId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String deleteShop(@PathVariable String shopId) {
        try {
            logger.info("Deleting shop with ID: {}", shopId);
            
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            shopRepository.delete(shop);
            
            logger.info("Shop deleted successfully");
            return "Shop deleted successfully";
        } catch (Exception e) {
            logger.error("Error deleting shop: {}", e.getMessage(), e);
            return "Error deleting shop: " + e.getMessage();
        }
    }

    @GetMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPosts(Model model, HttpServletRequest request) {
        try {
            logger.info("Loading admin posts page...");
            
            // Add current path to model
            model.addAttribute("currentPath", request.getRequestURI());
            
            // Get regular posts from MongoDB with only required fields
            List<Post> regularPosts = postRepository.findAll();
            logger.info("Regular posts loaded: {}", regularPosts != null ? regularPosts.size() : 0);
            
            // Get donation posts with only required fields
            List<DonationPostResponse> donationPosts = null;
            try {
                donationPosts = donationPostService.getAllDonationPosts();
                logger.info("Donation posts loaded: {}", donationPosts != null ? donationPosts.size() : 0);
            } catch (Exception e) {
                logger.error("Error loading donation posts: {}", e.getMessage(), e);
            }
            
            // Create separate lists for each type of post
            List<Map<String, Object>> formattedRegularPosts = new ArrayList<>();
            List<Map<String, Object>> formattedDonationPosts = new ArrayList<>();
            
            // Format regular posts
            if (regularPosts != null) {
                for (Post post : regularPosts) {
                    Map<String, Object> formattedPost = new HashMap<>();
                    formattedPost.put("id", post.getId());
                    formattedPost.put("title", post.getTitle());
                    formattedPost.put("description", post.getDescription());
                    formattedPost.put("status", post.getStatus());
                    formattedPost.put("category", post.getCategory());
                    formattedPost.put("date", post.getCreatedAt());
                    formattedRegularPosts.add(formattedPost);
                }
            }
            
            // Format donation posts
            if (donationPosts != null) {
                for (DonationPostResponse post : donationPosts) {
                    Map<String, Object> formattedPost = new HashMap<>();
                    formattedPost.put("id", post.getId());
                    formattedPost.put("title", post.getTitle());
                    formattedPost.put("description", post.getDescription());
                    formattedPost.put("targetAmount", post.getTargetAmount());
                    formattedPost.put("receivedAmount", post.getReceivedAmount());
                    formattedPost.put("endDate", post.getEndDate());
                    formattedPost.put("imageUrl", post.getImageUrl());
                    formattedPost.put("status", post.getStatus());
                    formattedPost.put("date", post.getCreatedAt());
                    formattedDonationPosts.add(formattedPost);
                }
            }
            
            // Get post authors
            Map<String, User> postAuthors = new HashMap<>();
            
            // Get authors for regular posts
            for (Post post : regularPosts) {
                if (post.getShopId() != null) {
                    Shop shop = shopRepository.findById(post.getShopId()).orElse(null);
                    if (shop != null && shop.getOwner() != null) {
                        userRepository.findById(shop.getOwner())
                            .ifPresent(user -> postAuthors.put(post.getId(), user));
                    }
                }
            }
            
            // Get authors for donation posts (using Admin as author)
            for (DonationPostResponse post : donationPosts) {
                User adminUser = new User();
                adminUser.setUsername("Admin");
                postAuthors.put(post.getId(), adminUser);
            }
            
            // Add to model
            model.addAttribute("regularPosts", formattedRegularPosts);
            model.addAttribute("donationPosts", formattedDonationPosts);
            model.addAttribute("postAuthors", postAuthors);
            
            logger.info("Admin posts page loaded successfully with {} regular posts and {} donation posts", 
                formattedRegularPosts.size(), formattedDonationPosts.size());
            
            return "admin/posts";
        } catch (Exception e) {
            logger.error("Error loading admin posts page: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> viewPost(@PathVariable String postId) {
        try {
            logger.info("Loading post details for ID: {}", postId);
            
            // Try to find the post in regular posts first
            Optional<Post> regularPost = postRepository.findById(postId);
            if (regularPost.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("_id", regularPost.get().getId());
                response.put("title", regularPost.get().getTitle());
                response.put("description", regularPost.get().getDescription());
                response.put("status", regularPost.get().getStatus());
                response.put("category", regularPost.get().getCategory());
                response.put("date", regularPost.get().getCreatedAt());
                return ResponseEntity.ok(response);
            }
            
            // If not found in regular posts, try donation posts
            List<DonationPostResponse> donationPosts = donationPostService.getAllDonationPosts();
            Optional<DonationPostResponse> donationPost = donationPosts.stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst();
                
            if (donationPost.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("_id", donationPost.get().getId());
                response.put("title", donationPost.get().getTitle());
                response.put("description", donationPost.get().getDescription());
                response.put("targetAmount", donationPost.get().getTargetAmount());
                response.put("receivedAmount", donationPost.get().getReceivedAmount());
                response.put("endDate", donationPost.get().getEndDate());
                response.put("imageUrl", donationPost.get().getImageUrl());
                response.put("status", donationPost.get().getStatus());
                response.put("date", donationPost.get().getCreatedAt());
                return ResponseEntity.ok(response);
            }
            
            logger.error("Post not found with ID: {}", postId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error loading post details: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/posts/{postId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String activatePost(@PathVariable String postId) {
        try {
            logger.info("Activating post with ID: {}", postId);
            
            Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
            
            post.setStatus("ACTIVE");
            postRepository.save(post);
            
            logger.info("Post activated successfully");
            return "Post activated successfully";
        } catch (Exception e) {
            logger.error("Error activating post: {}", e.getMessage(), e);
            return "Error activating post: " + e.getMessage();
        }
    }

    @PostMapping("/posts/{postId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String deactivatePost(@PathVariable String postId) {
        try {
            logger.info("Deactivating post with ID: {}", postId);
            
            Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
            
            post.setStatus("INACTIVE");
            postRepository.save(post);
            
            logger.info("Post deactivated successfully");
            return "Post deactivated successfully";
        } catch (Exception e) {
            logger.error("Error deactivating post: {}", e.getMessage(), e);
            return "Error deactivating post: " + e.getMessage();
        }
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String deletePost(@PathVariable String postId) {
        try {
            logger.info("Deleting post with ID: {}", postId);
            
            postRepository.deleteById(postId);
            
            logger.info("Post deleted successfully");
            return "Post deleted successfully";
        } catch (Exception e) {
            logger.error("Error deleting post: {}", e.getMessage(), e);
            return "Error deleting post: " + e.getMessage();
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewUsers(Model model) {
        try {
            logger.info("Loading users page...");
            
            List<User> users = userRepository.findAll();
            model.addAttribute("users", users);
            
            logger.info("Users page loaded successfully");
            return "admin/users";
        } catch (Exception e) {
            logger.error("Error loading users page: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/api/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> activateUser(@PathVariable String userId) {
        try {
            logger.info("Activating user with ID: {}", userId);
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setStatus("ACTIVE");
            userRepository.save(user);
            
            logger.info("User activated successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error activating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deactivateUser(@PathVariable String userId) {
        try {
            logger.info("Deactivating user with ID: {}", userId);
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setStatus("INACTIVE");
            userRepository.save(user);
            
            logger.info("User deactivated successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deactivating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/api/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            logger.info("Starting deletion process for user with ID: {}", userId);
            
            // First, verify the user exists
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Store all entities that need to be deleted for rollback
            List<Shop> shopsToDelete = new ArrayList<>();
            List<Post> postsToDelete = new ArrayList<>();
            
            // Find and collect all shops owned by the user
            List<Shop> userShops = shopRepository.findByOwner(userId);
            logger.info("Found {} shops owned by user {}", userShops.size(), userId);
            shopsToDelete.addAll(userShops);
            
            // For each shop, collect all associated posts
            for (Shop shop : userShops) {
                List<Post> shopPosts = postRepository.findByShopId(shop.getId());
                logger.info("Found {} posts for shop {}", shopPosts.size(), shop.getId());
                postsToDelete.addAll(shopPosts);
            }
            
            try {
                // Delete all posts first
                for (Post post : postsToDelete) {
                    postRepository.deleteById(post.getId());
                    logger.info("Deleted post with ID: {}", post.getId());
                }
                
                // Then delete all shops
                for (Shop shop : shopsToDelete) {
                    shopRepository.deleteById(shop.getId());
                    logger.info("Deleted shop with ID: {}", shop.getId());
                }
                
                // Finally, delete the user
                userRepository.deleteById(userId);
                logger.info("Deleted user with ID: {}", userId);
                
                return ResponseEntity.ok().body("User and all associated data deleted successfully");
            } catch (Exception e) {
                // If any deletion fails, the @Transactional annotation will rollback all changes
                logger.error("Error during deletion process: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to delete user and associated data. All changes have been rolled back.");
            }
        } catch (Exception e) {
            logger.error("Error in deleteUser: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/shops/{shopId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public String editShop(@PathVariable String shopId, @RequestBody Shop updatedShop) {
        try {
            logger.info("Editing shop with ID: {}", shopId);
            
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
            
            // Update shop details
            shop.setName(updatedShop.getName());
            shop.setLocation(updatedShop.getLocation());
            shop.setEmail(updatedShop.getEmail());
            shop.setPhone(updatedShop.getPhone());
            shop.setBusinessType(updatedShop.getBusinessType());
            shop.setDescription(updatedShop.getDescription());
            
            shopRepository.save(shop);
            
            logger.info("Shop edited successfully");
            return "Shop edited successfully";
        } catch (Exception e) {
            logger.error("Error editing shop: {}", e.getMessage(), e);
            return "Error editing shop: " + e.getMessage();
        }
    }

    @GetMapping("/api/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        try {
            logger.info("Fetching user with ID: {}", userId);
            
            if (userId == null || userId.trim().isEmpty()) {
                logger.error("Invalid user ID provided: {}", userId);
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.error("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            
            // Create a DTO to exclude sensitive information
            Map<String, Object> userDto = new HashMap<>();
            userDto.put("id", user.getId());
            userDto.put("username", user.getUsername());
            userDto.put("email", user.getEmail());
            userDto.put("role", user.getRole());
            userDto.put("status", user.getStatus());
            userDto.put("registrationDate", user.getRegistrationDate());
            
            logger.info("User fetched successfully: {}", userDto);
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            logger.error("Error fetching user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody Map<String, String> userData) {
        try {
            logger.info("Updating user with ID: {}", userId);
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update user fields
            if (userData.containsKey("username")) {
                user.setUsername(userData.get("username"));
            }
            if (userData.containsKey("email")) {
                user.setEmail(userData.get("email"));
            }
            if (userData.containsKey("role")) {
                user.setRole(userData.get("role"));
            }
            
            userRepository.save(user);
            
            logger.info("User updated successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> suspendUser(@PathVariable String userId) {
        try {
            logger.info("Suspending user with ID: {}", userId);
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setStatus(User.STATUS_INACTIVE);
            userRepository.save(user);
            
            logger.info("User suspended successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error suspending user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/posts/regular/{postId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> togglePostStatus(@PathVariable String postId) {
        try {
            logger.info("Toggling regular post status for ID: {}", postId);
            
            Optional<Post> regularPost = postRepository.findById(postId);
            if (regularPost.isPresent()) {
                Post post = regularPost.get();
                String currentStatus = post.getStatus();
                
                // Handle null or empty status
                if (currentStatus == null || currentStatus.trim().isEmpty()) {
                    currentStatus = "INACTIVE";
                }
                
                // Toggle between ACTIVE and INACTIVE
                String newStatus = currentStatus.equals("ACTIVE") ? "INACTIVE" : "ACTIVE";
                post.setStatus(newStatus);
                post.setUpdatedAt(LocalDateTime.now());
                postRepository.save(post);
                
                logger.info("Regular post status toggled from {} to {}", currentStatus, newStatus);
                return ResponseEntity.ok("Post status toggled successfully");
            }
            
            logger.error("Regular post not found with ID: {}", postId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error toggling regular post status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error toggling post status: " + e.getMessage());
        }
    }

    @PutMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updatePost(@PathVariable String postId, @RequestBody Map<String, Object> updates) {
        try {
            logger.info("Updating post with ID: {}", postId);
            
            // Try to find the post in regular posts first
            Optional<Post> regularPost = postRepository.findById(postId);
            if (regularPost.isPresent()) {
                Post post = regularPost.get();
                if (updates.containsKey("title")) {
                    post.setTitle((String) updates.get("title"));
                }
                if (updates.containsKey("description")) {
                    post.setDescription((String) updates.get("description"));
                }
                if (updates.containsKey("status")) {
                    post.setStatus((String) updates.get("status"));
                }
                if (updates.containsKey("category")) {
                    post.setCategory((String) updates.get("category"));
                }
                post.setUpdatedAt(LocalDateTime.now());
                postRepository.save(post);
                return ResponseEntity.ok("Post updated successfully");
            }
            
            // If not found in regular posts, try donation posts
            List<DonationPostResponse> donationPosts = donationPostService.getAllDonationPosts();
            Optional<DonationPostResponse> donationPost = donationPosts.stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst();
                
            if (donationPost.isPresent()) {
                // Update donation post through the service
                DonationPostRequest request = new DonationPostRequest();
                request.setTitle((String) updates.get("title"));
                request.setDescription((String) updates.get("description"));
                
                // Handle targetAmount conversion from either Integer or Double
                if (updates.containsKey("targetAmount")) {
                    Object targetAmount = updates.get("targetAmount");
                    if (targetAmount instanceof Integer) {
                        request.setTargetAmount(BigDecimal.valueOf(((Integer) targetAmount).doubleValue()));
                    } else if (targetAmount instanceof Double) {
                        request.setTargetAmount(BigDecimal.valueOf((Double) targetAmount));
                    } else if (targetAmount instanceof Number) {
                        request.setTargetAmount(BigDecimal.valueOf(((Number) targetAmount).doubleValue()));
                    }
                }
                
                // Parse String to LocalDate for endDate
                if (updates.containsKey("endDate")) {
                    String endDateStr = (String) updates.get("endDate");
                    request.setEndDate(LocalDate.parse(endDateStr));
                }
                
                if (updates.containsKey("imageUrl")) {
                    request.setImageUrl((String) updates.get("imageUrl"));
                }
                
                // Update donation post through the service
                donationPostService.updateDonationPost(postId, request);
                return ResponseEntity.ok("Donation post updated successfully");
            }
            
            logger.error("Post not found with ID: {}", postId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating post: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/posts/regular/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteRegularPost(@PathVariable String postId) {
        try {
            logger.info("Deleting regular post with ID: {}", postId);
            
            if (!postRepository.existsById(postId)) {
                logger.error("Regular post not found with ID: {}", postId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Regular post not found");
            }
            
            postRepository.deleteById(postId);
            logger.info("Regular post deleted successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting regular post: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while deleting the regular post");
        }
    }
} 