package com.example.tour_place_api.service;

import com.example.tour_place_api.model.entity.Place;
import com.example.tour_place_api.model.entity.AdditionalImage;
import com.example.tour_place_api.model.entity.ImageOfPlace;
import com.example.tour_place_api.model.request.CreatePlaceRequest;
import com.example.tour_place_api.model.request.UpdatePlaceRequest;
import com.example.tour_place_api.model.response.PlaceResponse;
import com.example.tour_place_api.model.response.PlaceSummaryResponse;
import com.example.tour_place_api.repository.mapper.PlaceMapper;
import com.example.tour_place_api.repository.mapper.AdditionalImageMapper;
import com.example.tour_place_api.repository.mapper.ImageOfPlaceMapper;
import com.example.tour_place_api.repository.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceService {
    @Autowired
    private PlaceMapper placeMapper;

    @Autowired
    private AdditionalImageMapper additionalImageMapper;

    @Autowired
    private ImageOfPlaceMapper imageOfPlaceMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private FavoriteMapper favoriteMapper;

    public PlaceResponse createPlace(CreatePlaceRequest request, MultipartFile mainImage) {
        Place place = Place.builder()
                .placeId(UUID.randomUUID())
                .placeName(request.getPlaceName())
                .description(request.getDescription())
                .lat(request.getLat())
                .longitude(request.getLongitude())
                .build();

        // Priority: MultipartFile > mainImageUrl from request
        if (mainImage != null && !mainImage.isEmpty()) {
            // Upload file to MinIO and get URL
            String mainImageUrl = minioService.uploadFile(mainImage);
            // Set the URL to the place entity (will be saved to database)
            place.setMainImage(mainImageUrl);
        } else if (request.getMainImageUrl() != null && !request.getMainImageUrl().trim().isEmpty()) {
            // Use provided URL directly (from file upload endpoint)
            place.setMainImage(request.getMainImageUrl());
        }

        // Insert place with image URL into database
        placeMapper.insert(place);
        return mapToResponse(place, null, null);
    }

    public PlaceResponse getPlaceById(UUID placeId, UUID userId) {
        Optional<Place> placeOptional = placeMapper.findById(placeId);
        
        if (placeOptional.isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        Place place = placeOptional.get();
        List<String> additionalImages = getAdditionalImagesForPlace(placeId);
        
        // Check if place is favorited by user (null if userId is null)
        Boolean isFavorite = null;
        if (userId != null) {
            isFavorite = favoriteMapper.findByUserAndPlace(userId, placeId).isPresent();
        }
        
        return mapToResponse(place, additionalImages, isFavorite);
    }

    public List<PlaceResponse> getAllPlaces(String filter, UUID userId) {
        List<Place> places;
        if ("most_favorite".equalsIgnoreCase(filter)) {
            places = placeMapper.findAllOrderByFavoriteCount();
        } else {
            places = placeMapper.findAll();
        }
        
        return places.stream()
                .map(place -> {
                    List<String> additionalImages = getAdditionalImagesForPlace(place.getPlaceId());
                    // Check if place is favorited by user (null if userId is null, false if not favorited)
                    Boolean isFavorite = null;
                    if (userId != null) {
                        isFavorite = favoriteMapper.findByUserAndPlace(userId, place.getPlaceId()).isPresent();
                    }
                    return mapToResponse(place, additionalImages, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public List<PlaceResponse> getNearbyPlaces(double lat, double longitude, int limit, UUID userId) {
        List<Place> places = placeMapper.findNearby(lat, longitude, limit);
        return places.stream()
                .map(place -> {
                    List<String> additionalImages = getAdditionalImagesForPlace(place.getPlaceId());
                    // Check if place is favorited by user (null if userId is null, false if not favorited)
                    Boolean isFavorite = null;
                    if (userId != null) {
                        isFavorite = favoriteMapper.findByUserAndPlace(userId, place.getPlaceId()).isPresent();
                    }
                    return mapToResponse(place, additionalImages, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public List<PlaceResponse> searchPlaces(String searchTerm) {
        return placeMapper.searchByName("%" + searchTerm + "%")
                .stream()
                .map(place -> {
                    List<String> additionalImages = getAdditionalImagesForPlace(place.getPlaceId());
                    return mapToResponse(place, additionalImages, null);
                })
                .collect(Collectors.toList());
    }

    public List<PlaceSummaryResponse> searchPlacesSummary(String searchTerm) {
        return placeMapper.searchByName("%" + searchTerm + "%")
                .stream()
                .map(place -> PlaceSummaryResponse.builder()
                        .placeId(place.getPlaceId())
                        .placeName(place.getPlaceName())
                        .build())
                .collect(Collectors.toList());
    }

    public PlaceResponse updatePlace(UUID placeId, UpdatePlaceRequest request) {
        Optional<Place> placeOptional = placeMapper.findById(placeId);
        
        if (placeOptional.isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        Place place = placeOptional.get();

        if (request.getPlaceName() != null) {
            place.setPlaceName(request.getPlaceName());
        }
        if (request.getDescription() != null) {
            place.setDescription(request.getDescription());
        }
        if (request.getLat() != null) {
            place.setLat(request.getLat());
        }
        if (request.getLongitude() != null) {
            place.setLongitude(request.getLongitude());
        }

        // Use mainImageUrl from request if provided (from file upload endpoint)
        if (request.getMainImageUrl() != null && !request.getMainImageUrl().trim().isEmpty()) {
            place.setMainImage(request.getMainImageUrl().trim());
        }

        Optional<Place> updatedPlaceOptional = placeMapper.update(place);
        if (updatedPlaceOptional.isEmpty()) {
            throw new RuntimeException("Place not found");
        }
        
        Place updatedPlace = updatedPlaceOptional.get();
        return mapToResponse(updatedPlace, getAdditionalImagesForPlace(placeId), null);
    }

    public void deletePlace(UUID placeId) {
        if (placeMapper.findById(placeId).isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        imageOfPlaceMapper.deleteByPlaceId(placeId);
        placeMapper.delete(placeId);
    }

    public void addAdditionalImages(UUID placeId, List<String> imageUrls) {
        if (placeMapper.findById(placeId).isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        for (String imageUrl : imageUrls) {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                continue; // Skip empty URLs
            }

            AdditionalImage additionalImage = AdditionalImage.builder()
                    .imageId(UUID.randomUUID())
                    .imageUrl(imageUrl.trim())
                    .build();

            additionalImageMapper.insert(additionalImage);

            ImageOfPlace imageOfPlace = ImageOfPlace.builder()
                    .ipId(UUID.randomUUID())
                    .placeId(placeId)
                    .imageId(additionalImage.getImageId())
                    .build();

            imageOfPlaceMapper.insert(imageOfPlace);
        }
    }

    private List<String> getAdditionalImagesForPlace(UUID placeId) {
        return imageOfPlaceMapper.findByPlaceId(placeId)
                .stream()
                .map(imageOfPlace -> {
                    Optional<AdditionalImage> imageOptional = additionalImageMapper.findById(imageOfPlace.getImageId());
                    return imageOptional.map(AdditionalImage::getImageUrl).orElse(null);
                })
                .filter(url -> url != null)
                .collect(Collectors.toList());
    }

    private PlaceResponse mapToResponse(Place place, List<String> additionalImages, Boolean isFavorite) {
        return PlaceResponse.builder()
                .placeId(place.getPlaceId())
                .placeName(place.getPlaceName())
                .description(place.getDescription())
                .mainImage(place.getMainImage())
                .lat(place.getLat())
                .longitude(place.getLongitude())
                .additionalImages(additionalImages)
                .isFavorite(isFavorite)
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }
}
