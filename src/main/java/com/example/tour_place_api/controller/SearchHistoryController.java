package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.request.CreateSearchHistoryRequest;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.SearchHistoryResponse;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import com.example.tour_place_api.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/search-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchHistoryController {
    @Autowired
    private SearchHistoryService searchHistoryService;

    @Operation(summary = "Create or update search history", 
               description = "Create a new search history entry or update existing one's timestamp. If a search history already exists for this user and place, it updates the updated_at timestamp to make it the latest. (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ApiResponse<SearchHistoryResponse>> createSearchHistory(
            @Valid @RequestBody CreateSearchHistoryRequest request,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            SearchHistoryResponse history = searchHistoryService.createOrUpdateSearchHistory(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<SearchHistoryResponse>builder()
                            .success(true)
                            .message("Search history created/updated successfully")
                            .payload(history)
                            .status(HttpStatus.CREATED)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<SearchHistoryResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Get latest search history", 
               description = "Get the 10 latest search history entries for the authenticated user, ordered by updated_at descending. (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchHistoryResponse>>> getLatestSearchHistory(
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            List<SearchHistoryResponse> histories = searchHistoryService.getLatestSearchHistory(userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<SearchHistoryResponse>>builder()
                            .success(true)
                            .message("Search history retrieved successfully")
                            .payload(histories)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<SearchHistoryResponse>>builder()
                            .success(false)
                            .message("Error retrieving search history: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Update search history timestamp", 
               description = "Update the updated_at timestamp of a search history entry to make it the latest. (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateSearchHistoryTimestamp(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            searchHistoryService.updateSearchHistoryTimestamp(id, userId);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Search history timestamp updated successfully")
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

    @Operation(summary = "Delete search history", 
               description = "Delete a search history entry. Users can only delete their own search history. (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSearchHistory(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            searchHistoryService.deleteSearchHistory(id, userId);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Search history deleted successfully")
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
}

