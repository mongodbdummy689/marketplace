package com.marketplace.service;

import com.marketplace.dto.DonationPostRequest;
import com.marketplace.dto.DonationPostResponse;
import java.util.List;

public interface DonationPostService {
    DonationPostResponse createDonationPost(DonationPostRequest request);
    List<DonationPostResponse> getAllDonationPosts();
    DonationPostResponse getDonationPostById(String id);
    DonationPostResponse updateDonationPost(String id, DonationPostRequest request);
    void deleteDonationPost(String id);
    void updateDonationStatus();
    void togglePostStatus(String id);
} 