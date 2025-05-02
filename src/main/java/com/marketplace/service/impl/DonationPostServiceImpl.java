package com.marketplace.service.impl;

import com.marketplace.dto.DonationPostRequest;
import com.marketplace.dto.DonationPostResponse;
import com.marketplace.entity.DonationPost;
import com.marketplace.repository.DonationPostRepository;
import com.marketplace.service.DonationPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationPostServiceImpl implements DonationPostService {

    @Autowired
    private DonationPostRepository donationPostRepository;

    @Override
    public DonationPostResponse createDonationPost(DonationPostRequest request) {
        DonationPost donationPost = new DonationPost();
        donationPost.setTitle(request.getTitle());
        donationPost.setDescription(request.getDescription());
        donationPost.setTargetAmount(request.getTargetAmount());
        donationPost.setEndDate(request.getEndDate());
        donationPost.setImageUrl(request.getImageUrl());
        
        DonationPost savedPost = donationPostRepository.save(donationPost);
        return convertToResponse(savedPost);
    }

    @Override
    public List<DonationPostResponse> getAllDonationPosts() {
        return donationPostRepository.findByType("DONATION")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DonationPostResponse getDonationPostById(String id) {
        return donationPostRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Donation post not found"));
    }

    @Override
    public DonationPostResponse updateDonationPost(String id, DonationPostRequest request) {
        DonationPost donationPost = donationPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation post not found"));

        donationPost.setTitle(request.getTitle());
        donationPost.setDescription(request.getDescription());
        donationPost.setTargetAmount(request.getTargetAmount());
        donationPost.setEndDate(request.getEndDate());
        donationPost.setImageUrl(request.getImageUrl());
        donationPost.setUpdatedAt(LocalDateTime.now());

        DonationPost updatedPost = donationPostRepository.save(donationPost);
        return convertToResponse(updatedPost);
    }

    @Override
    public void deleteDonationPost(String id) {
        if (!donationPostRepository.existsById(id)) {
            throw new RuntimeException("Donation post not found");
        }
        donationPostRepository.deleteById(id);
    }

    @Override
    public void updateDonationStatus() {
        LocalDate today = LocalDate.now();
        List<DonationPost> expiredPosts = donationPostRepository.findByEndDateBeforeAndStatus(today, "ACTIVE");
        
        expiredPosts.forEach(post -> {
            post.setStatus("EXPIRED");
            post.setUpdatedAt(LocalDateTime.now());
        });
        
        donationPostRepository.saveAll(expiredPosts);
    }

    @Override
    public void togglePostStatus(String id) {
        DonationPost donationPost = donationPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation post not found"));

        String currentStatus = donationPost.getStatus();
        String newStatus = "ACTIVE".equals(currentStatus) ? "INACTIVE" : "ACTIVE";
        
        donationPost.setStatus(newStatus);
        donationPost.setUpdatedAt(LocalDateTime.now());
        
        donationPostRepository.save(donationPost);
    }

    private DonationPostResponse convertToResponse(DonationPost donationPost) {
        DonationPostResponse response = new DonationPostResponse();
        response.setId(donationPost.getId());
        response.setTitle(donationPost.getTitle());
        response.setDescription(donationPost.getDescription());
        response.setTargetAmount(donationPost.getTargetAmount());
        response.setReceivedAmount(donationPost.getReceivedAmount());
        response.setEndDate(donationPost.getEndDate());
        response.setImageUrl(donationPost.getImageUrl());
        response.setStatus(donationPost.getStatus());
        response.setCreatedAt(donationPost.getCreatedAt());
        response.setUpdatedAt(donationPost.getUpdatedAt());
        response.setType(donationPost.getType());
        return response;
    }
} 