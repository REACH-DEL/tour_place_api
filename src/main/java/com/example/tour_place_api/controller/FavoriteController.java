package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.FavoriteResponse;
import com.example.tour_place_api.service.FavoriteService;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @Operation(summary = "Add favorite", description = "Add a place to user favorites (Requires ROLE_USER)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{placeId}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @PathVariable UUID placeId,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            FavoriteResponse favorite = favoriteService.addFavorite(userId, placeId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<FavoriteResponse>builder()
                            .success(true)
                            .message("Place added to favorites")
                            .payload(favorite)
                            .status(HttpStatus.CREATED)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<FavoriteResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Get favorites", description = "Get all user favorites (Requires ROLE_USER)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavorites(Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            List<FavoriteResponse> favorites = favoriteService.getFavorites(userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<FavoriteResponse>>builder()
                            .success(true)
                            .message("Favorites retrieved successfully")
                            .payload(favorites)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<FavoriteResponse>>builder()
                            .success(false)
                            .message("Error retrieving favorites: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Remove favorite", description = "Remove a place from user favorites (Requires ROLE_USER)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{placeId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable UUID placeId,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            favoriteService.removeFavorite(userId, placeId);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Place removed from favorites")
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
    }

    @Operation(summary = "Check favorite", description = "Check if a place is in user favorites (Requires ROLE_USER)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/check/{placeId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(
            @PathVariable UUID placeId,
            Authentication authentication) {
        try {   
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            boolean isFavorite = favoriteService.isFavorite(userId, placeId);
            return ResponseEntity.ok(
                    ApiResponse.<Boolean>builder()
                            .success(true)
                            .message("Check completed")
                            .payload(isFavorite)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Boolean>builder()
                            .success(false)
                            .message("Error checking favorite: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }
}
