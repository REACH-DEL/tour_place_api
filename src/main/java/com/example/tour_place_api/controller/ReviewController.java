package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.request.CreateReviewRequest;
import com.example.tour_place_api.model.request.UpdateReviewRequest;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.PlaceRatingResponse;
import com.example.tour_place_api.model.response.ReviewResponse;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import com.example.tour_place_api.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "Create review", description = "Create a review for a place. One review per user per place. (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            ReviewResponse review = reviewService.createReview(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .success(true)
                            .message("Review created successfully")
                            .payload(review)
                            .status(HttpStatus.CREATED)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Update review", description = "Update your own review (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            ReviewResponse review = reviewService.updateReview(reviewId, userId, request);
            return ResponseEntity.ok(
                    ApiResponse.<ReviewResponse>builder()
                            .success(true)
                            .message("Review updated successfully")
                            .payload(review)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Delete review", description = "Delete your own review or admin can delete any review (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());
            String userRole = details.getRole();

            reviewService.deleteReview(reviewId, userId, userRole);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Review deleted successfully")
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Get reviews by place", description = "Get all reviews for a specific place (Public endpoint)", security = {})
    @GetMapping("/place/{placeId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByPlace(
            @Parameter(description = "Place ID to get reviews for", required = true)
            @PathVariable UUID placeId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByPlaceId(placeId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ReviewResponse>>builder()
                            .success(true)
                            .message("Reviews retrieved successfully")
                            .payload(reviews)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ReviewResponse>>builder()
                            .success(false)
                            .message("Error retrieving reviews: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Get reviews by user", description = "Get all reviews by a specific user (Public endpoint)", security = {})
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByUser(
            @Parameter(description = "User ID to get reviews for", required = true)
            @PathVariable UUID userId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ReviewResponse>>builder()
                            .success(true)
                            .message("Reviews retrieved successfully")
                            .payload(reviews)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ReviewResponse>>builder()
                            .success(false)
                            .message("Error retrieving reviews: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Get review by ID", description = "Get a specific review by ID (Public endpoint)", security = {})
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", required = true)
            @PathVariable UUID reviewId) {
        try {
            ReviewResponse review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(
                    ApiResponse.<ReviewResponse>builder()
                            .success(true)
                            .message("Review retrieved successfully")
                            .payload(review)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<ReviewResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
    }

    @Operation(summary = "Get place rating statistics", description = "Get average rating and rating distribution for a place (Public endpoint)", security = {})
    @GetMapping("/place/{placeId}/rating")
    public ResponseEntity<ApiResponse<PlaceRatingResponse>> getPlaceRating(
            @Parameter(description = "Place ID to get rating for", required = true)
            @PathVariable UUID placeId) {
        try {
            PlaceRatingResponse rating = reviewService.getPlaceRating(placeId);
            return ResponseEntity.ok(
                    ApiResponse.<PlaceRatingResponse>builder()
                            .success(true)
                            .message("Place rating retrieved successfully")
                            .payload(rating)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PlaceRatingResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
    }
}

