package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.request.AddImagesRequest;
import com.example.tour_place_api.model.request.CreatePlaceRequest;
import com.example.tour_place_api.model.request.UpdatePlaceRequest;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.PlaceResponse;
import com.example.tour_place_api.model.response.PlaceSummaryResponse;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import com.example.tour_place_api.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlaceController {
    @Autowired
    private PlaceService placeService;

    @Operation(summary = "Create place", description = "Create a new place. Optionally include mainImageUrl (get URL from /api/v1/files/upload endpoint). (Requires ROLE_ADMIN)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<PlaceResponse>> createPlace(
            @Valid @RequestBody CreatePlaceRequest request) {
        try {
            PlaceResponse place = placeService.createPlace(request, null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<PlaceResponse>builder()
                            .success(true)
                            .message("Place created successfully")
                            .payload(place)
                            .status(HttpStatus.CREATED)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<PlaceResponse>builder()
                            .success(false)
                            .message("Error creating place: " + e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Get all places", description = "Get all places (Public endpoint)", security = {})
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getAllPlaces() {
        try {
            List<PlaceResponse> places = placeService.getAllPlaces();
            return ResponseEntity.ok(
                    ApiResponse.<List<PlaceResponse>>builder()
                            .success(true)
                            .message("Places retrieved successfully")
                            .payload(places)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PlaceResponse>>builder()
                            .success(false)
                            .message("Error retrieving places: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Get place by ID", description = "Get a place by ID. If authenticated, includes isFavorite field. (Public endpoint, optional authentication)", security = {})
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaceResponse>> getPlaceById(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            // Extract userId if authenticated, otherwise null
            UUID userId = null;
            if (authentication != null && authentication.isAuthenticated() && authentication.getDetails() instanceof JwtAuthenticationDetails) {
                JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
                userId = UUID.fromString(details.getUserId());
            }
            
            PlaceResponse place = placeService.getPlaceById(id, userId);
            return ResponseEntity.ok(
                    ApiResponse.<PlaceResponse>builder()
                            .success(true)
                            .message("Place retrieved successfully")
                            .payload(place)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PlaceResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
    }

    @Operation(summary = "Search places", description = "Search places by name. Returns lightweight results with only placeId and placeName (like YouTube search). (Public endpoint)", security = {})
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PlaceSummaryResponse>>> searchPlaces(
            @Parameter(description = "Search query to find places by name", required = true, example = "Khmer")
            @RequestParam("query") String query) {
        try {
            List<PlaceSummaryResponse> places = placeService.searchPlacesSummary(query);
            return ResponseEntity.ok(
                    ApiResponse.<List<PlaceSummaryResponse>>builder()
                            .success(true)
                            .message("Search results retrieved successfully")
                            .payload(places)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PlaceSummaryResponse>>builder()
                            .success(false)
                            .message("Error searching places: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Update place", description = "Update an existing place. Optionally include mainImageUrl (get URL from /api/v1/file/upload endpoint). (Requires ROLE_ADMIN)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<ApiResponse<PlaceResponse>> updatePlace(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlaceRequest request) {
        try {
            PlaceResponse place = placeService.updatePlace(id, request);
            return ResponseEntity.ok(
                    ApiResponse.<PlaceResponse>builder()
                            .success(true)
                            .message("Place updated successfully")
                            .payload(place)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PlaceResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
    }

    @Operation(summary = "Delete place", description = "Delete a place (Requires ROLE_ADMIN)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlace(@PathVariable UUID id) {
        try {
            placeService.deletePlace(id);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Place deleted successfully")
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

    @Operation(summary = "Add images to place", description = "Add additional images to a place by providing a list of image URLs (Requires ROLE_ADMIN)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/{id}/images", consumes = "application/json")
    public ResponseEntity<ApiResponse<Void>> addAdditionalImages(
            @PathVariable UUID id,
            @Valid @RequestBody AddImagesRequest request) {
        try {
            placeService.addAdditionalImages(id, request.getImageUrls());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<Void>builder()
                            .success(true)
                            .message("Images added successfully")
                            .status(HttpStatus.CREATED)
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
}
