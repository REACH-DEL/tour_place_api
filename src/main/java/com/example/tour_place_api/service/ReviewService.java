package com.example.tour_place_api.service;

import com.example.tour_place_api.model.entity.Review;
import com.example.tour_place_api.model.entity.Place;
import com.example.tour_place_api.model.entity.User;
import com.example.tour_place_api.model.request.CreateReviewRequest;
import com.example.tour_place_api.model.request.UpdateReviewRequest;
import com.example.tour_place_api.model.response.ReviewResponse;
import com.example.tour_place_api.model.response.PlaceRatingResponse;
import com.example.tour_place_api.repository.mapper.ReviewMapper;
import com.example.tour_place_api.repository.mapper.PlaceMapper;
import com.example.tour_place_api.repository.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private PlaceMapper placeMapper;

    @Autowired
    private UserMapper userMapper;

    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        // Check if place exists
        Optional<Place> placeOptional = placeMapper.findById(request.getPlaceId());
        if (placeOptional.isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        // Check if user already reviewed this place
        Optional<Review> existingReview = reviewMapper.findByUserAndPlace(userId, request.getPlaceId());
        if (existingReview.isPresent()) {
            throw new RuntimeException("You have already reviewed this place. Use update endpoint to modify your review.");
        }

        UUID reviewId = UUID.randomUUID();
        Review review = Review.builder()
                .reviewId(reviewId)
                .userId(userId)
                .placeId(request.getPlaceId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewMapper.insert(review);
        
        // Fetch the saved review to get timestamps
        Optional<Review> savedReview = reviewMapper.findById(reviewId);
        if (savedReview.isEmpty()) {
            throw new RuntimeException("Failed to create review");
        }
        
        return mapToResponse(savedReview.get());
    }

    public ReviewResponse updateReview(UUID reviewId, UUID userId, UpdateReviewRequest request) {
        Optional<Review> reviewOptional = reviewMapper.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOptional.get();
        
        // Check if user owns this review
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("You can only update your own reviews");
        }

        Optional<Review> updatedReview = reviewMapper.update(
                reviewId,
                request.getRating() != null ? request.getRating() : review.getRating(),
                request.getComment() != null ? request.getComment() : review.getComment()
        );

        if (updatedReview.isEmpty()) {
            throw new RuntimeException("Failed to update review");
        }

        return mapToResponse(updatedReview.get());
    }

    public void deleteReview(UUID reviewId, UUID userId, String userRole) {
        Optional<Review> reviewOptional = reviewMapper.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOptional.get();
        
        // Check if user owns this review or is admin
        if (!review.getUserId().equals(userId) && !"admin".equalsIgnoreCase(userRole)) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        reviewMapper.delete(reviewId);
    }

    public List<ReviewResponse> getReviewsByPlaceId(UUID placeId) {
        List<Review> reviews = reviewMapper.findByPlaceId(placeId);
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsByUserId(UUID userId) {
        List<Review> reviews = reviewMapper.findByUserId(userId);
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReviewResponse getReviewById(UUID reviewId) {
        Optional<Review> reviewOptional = reviewMapper.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            throw new RuntimeException("Review not found");
        }
        return mapToResponse(reviewOptional.get());
    }

    public PlaceRatingResponse getPlaceRating(UUID placeId) {
        Optional<Place> placeOptional = placeMapper.findById(placeId);
        if (placeOptional.isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        Place place = placeOptional.get();
        Double averageRating = reviewMapper.getAverageRatingByPlaceId(placeId);
        Long totalReviews = reviewMapper.getTotalReviewsByPlaceId(placeId);
        Long ratingCount1 = reviewMapper.getRatingCountByPlaceIdAndRating(placeId, 1);
        Long ratingCount2 = reviewMapper.getRatingCountByPlaceIdAndRating(placeId, 2);
        Long ratingCount3 = reviewMapper.getRatingCountByPlaceIdAndRating(placeId, 3);
        Long ratingCount4 = reviewMapper.getRatingCountByPlaceIdAndRating(placeId, 4);
        Long ratingCount5 = reviewMapper.getRatingCountByPlaceIdAndRating(placeId, 5);

        return PlaceRatingResponse.builder()
                .placeId(placeId)
                .placeName(place.getPlaceName())
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .ratingCount1(ratingCount1 != null ? ratingCount1 : 0L)
                .ratingCount2(ratingCount2 != null ? ratingCount2 : 0L)
                .ratingCount3(ratingCount3 != null ? ratingCount3 : 0L)
                .ratingCount4(ratingCount4 != null ? ratingCount4 : 0L)
                .ratingCount5(ratingCount5 != null ? ratingCount5 : 0L)
                .build();
    }

    private ReviewResponse mapToResponse(Review review) {
        Optional<User> userOptional = userMapper.findById(review.getUserId());
        Optional<Place> placeOptional = placeMapper.findById(review.getPlaceId());

        String userName = userOptional.map(User::getFullName).orElse("Unknown User");
        String userEmail = userOptional.map(User::getEmail).orElse("");
        String userProfileImage = userOptional.map(User::getProfileImage).orElse(null);
        String placeName = placeOptional.map(Place::getPlaceName).orElse("Unknown Place");

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUserId())
                .userName(userName)
                .userEmail(userEmail)
                .userProfileImage(userProfileImage)
                .placeId(review.getPlaceId())
                .placeName(placeName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}

