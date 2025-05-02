package com.marketplace.controller;

import com.marketplace.dto.DonationPostRequest;
import com.marketplace.dto.DonationPostResponse;
import com.marketplace.service.DonationPostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/donation-posts")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminDonationPostController {

    private static final Logger log = LoggerFactory.getLogger(AdminDonationPostController.class);

    @Autowired
    private DonationPostService donationPostService;

    @PostMapping("/create")
    public ResponseEntity<DonationPostResponse> createDonationPost(@RequestBody DonationPostRequest request) {
        DonationPostResponse response = donationPostService.createDonationPost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<Void> togglePostStatus(@PathVariable String id) {
        donationPostService.togglePostStatus(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable String id) {
        try {
            log.info("Attempting to delete donation post with ID: {}", id);
            donationPostService.deleteDonationPost(id);
            log.info("Successfully deleted donation post with ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting donation post with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting donation post with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while deleting the donation post");
        }
    }
} 