package com.marketplace.controller;

import com.marketplace.dto.DonationPostRequest;
import com.marketplace.dto.DonationPostResponse;
import com.marketplace.service.DonationPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donation-posts")
public class DonationPostController {

    @Autowired
    private DonationPostService donationPostService;

    @PostMapping
    public ResponseEntity<DonationPostResponse> createDonationPost(@RequestBody DonationPostRequest request) {
        DonationPostResponse response = donationPostService.createDonationPost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DonationPostResponse>> getAllDonationPosts() {
        List<DonationPostResponse> responses = donationPostService.getAllDonationPosts();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationPostResponse> getDonationPostById(@PathVariable String id) {
        DonationPostResponse response = donationPostService.getDonationPostById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonationPostResponse> updateDonationPost(
            @PathVariable String id,
            @RequestBody DonationPostRequest request) {
        DonationPostResponse response = donationPostService.updateDonationPost(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonationPost(@PathVariable String id) {
        donationPostService.deleteDonationPost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/update-status")
    public ResponseEntity<Void> updateDonationStatus() {
        donationPostService.updateDonationStatus();
        return new ResponseEntity<>(HttpStatus.OK);
    }
} 