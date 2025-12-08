package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.request.AddImagesRequest;
import com.example.tour_place_api.model.request.CreatePlaceRequest;
import com.example.tour_place_api.model.request.UpdatePlaceRequest;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.PlaceResponse;
import com.example.tour_place_api.model.response.PlaceSummaryResponse;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import com.example.tour_place_api.service.PlaceService;
import com.example.tour_place_api.service.DashboardService;
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

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "Create place", description = "Create a new place. Optionally include mainImageUrl (get URL from /api/v1/files/upload endpoint). (Requires ROLE_ADMIN)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<PlaceResponse>> createPlace(
            @Valid @RequestBody CreatePlaceRequest request,
            Authentication authentication) {
        try {
            PlaceResponse place = placeService.createPlace(request, null);
            
            // Log activity
            if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationDetails) {
                JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
                UUID userId = UUID.fromString(details.getUserId());
                dashboardService.logActivity("PLACE_CREATED", "PLACE", place.getPlaceId(), place.getPlaceName(), userId);
            }
            
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

    @Operation(summary = "Get all places", description = "Get all places. Filter by 'most_favorite' to get places ordered by favorite count, or 'all' (or omit) for all places. If authenticated, includes isFavorite field. (Public endpoint, optional authentication)", security = {})
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getAllPlaces(
            @Parameter(description = "Filter: 'most_favorite' to order by favorite count, 'all' or omit for all places", required = false, example = "all")
            @RequestParam(value = "filter", required = false, defaultValue = "all") String filter,
            Authentication authentication) {
        try {
            // Extract userId if authenticated, otherwise null
            UUID userId = null;
            if (authentication != null && authentication.isAuthenticated() && authentication.getDetails() instanceof JwtAuthenticationDetails) {
                JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
                userId = UUID.fromString(details.getUserId());
            }
            
            List<PlaceResponse> places = placeService.getAllPlaces(filter, userId);
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

    @Operation(summary = "Get nearby places", description = "Get places near a specific location using latitude and longitude. Returns places ordered by distance (in kilometers). If authenticated, includes isFavorite field. (Public endpoint, optional authentication)", security = {})
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getNearbyPlaces(
            @Parameter(description = "Latitude of the location", required = true, example = "13.4125")
            @RequestParam("lat") double lat,
            @Parameter(description = "Longitude of the location", required = true, example = "103.8670")
            @RequestParam("longitude") double longitude,
            @Parameter(description = "Maximum number of results to return (1-100)", required = false, example = "10")
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            Authentication authentication) {
        try {
            // Validate limit
            if (limit < 1) limit = 1;
            if (limit > 100) limit = 100;
            
            // Validate coordinates
            if (lat < -90 || lat > 90) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<List<PlaceResponse>>builder()
                                .success(false)
                                .message("Latitude must be between -90 and 90")
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }
            if (longitude < -180 || longitude > 180) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.<List<PlaceResponse>>builder()
                                .success(false)
                                .message("Longitude must be between -180 and 180")
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }
            
            // Extract userId if authenticated, otherwise null
            UUID userId = null;
            if (authentication != null && authentication.isAuthenticated() && authentication.getDetails() instanceof JwtAuthenticationDetails) {
                JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
                userId = UUID.fromString(details.getUserId());
            }
            
            List<PlaceResponse> places = placeService.getNearbyPlaces(lat, longitude, limit, userId);
            return ResponseEntity.ok(
                    ApiResponse.<List<PlaceResponse>>builder()
                            .success(true)
                            .message("Nearby places retrieved successfully")
                            .payload(places)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PlaceResponse>>builder()
                            .success(false)
                            .message("Error retrieving nearby places: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Update place", description = "Update an existing place. Optionally include mainImageUrl (get URL from /api/v1/file/upload endpoint). (Requires ROLE_ADMIN)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<ApiResponse<PlaceResponse>> updatePlace(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlaceRequest request,
            Authentication authentication) {
        try {
            PlaceResponse place = placeService.updatePlace(id, request);
            
            // Log activity
            if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationDetails) {
                JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
                UUID userId = UUID.fromString(details.getUserId());
                dashboardService.logActivity("PLACE_UPDATED", "PLACE", place.getPlaceId(), place.getPlaceName(), userId);
            }
            
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
    public ResponseEntity<ApiResponse<Void>> deletePlace(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            // Get place name before deletion for activity log
            String placeName = "Unknown";
            try {
                PlaceResponse place = placeService.getPlaceById(id, null);
                placeName = place.getPlaceName();
            } catch (Exception e) {
                // Place not found, use default name
            }
            
            placeService.deletePlace(id);
            
            // Log activity
            if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationDetails) {
                JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
                UUID userId = UUID.fromString(details.getUserId());
                dashboardService.logActivity("PLACE_DELETED", "PLACE", id, placeName, userId);
            }
            
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
            @Valid @RequestBody AddImagesRequest request,
            Authentication authentication) {
        try {
            placeService.addAdditionalImages(id, request.getImageUrls());
            
            // Log activity for each image uploaded
            if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationDetails) {
                JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
                UUID userId = UUID.fromString(details.getUserId());
                try {
                    PlaceResponse place = placeService.getPlaceById(id, null);
                    // Log activity once for the batch of images
                    dashboardService.logActivity("IMAGE_UPLOADED", "IMAGE", id, place.getPlaceName(), userId);
                } catch (Exception e) {
                    // Log without place name if place not found
                    dashboardService.logActivity("IMAGE_UPLOADED", "IMAGE", id, "Unknown", userId);
                }
            }
            
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
