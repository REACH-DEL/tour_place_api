package com.example.tour_place_api.service;

import com.example.tour_place_api.model.entity.Favorite;
import com.example.tour_place_api.model.entity.Place;
import com.example.tour_place_api.model.response.FavoriteResponse;
import com.example.tour_place_api.repository.mapper.FavoriteMapper;
import com.example.tour_place_api.repository.mapper.PlaceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private PlaceMapper placeMapper;

    public FavoriteResponse addFavorite(UUID userId, UUID placeId) {
        Optional<Favorite> existingFavorite = favoriteMapper.findByUserAndPlace(userId, placeId);
        if (existingFavorite.isPresent()) {
            throw new RuntimeException("Place is already in favorites");
        }

        if (placeMapper.findById(placeId).isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        Favorite favorite = Favorite.builder()
                .favId(UUID.randomUUID())
                .userId(userId)
                .placeId(placeId)
                .build();

        favoriteMapper.insert(favorite);

        Optional<Place> place = placeMapper.findById(placeId);
        return mapToResponse(favorite, place.get());
    }

    public List<FavoriteResponse> getFavorites(UUID userId) {
        return favoriteMapper.findByUserId(userId)
                .stream()
                .map(favorite -> {
                    Optional<Place> place = placeMapper.findById(favorite.getPlaceId());
                    return place.map(p -> mapToResponse(favorite, p)).orElse(null);
                })
                .filter(fav -> fav != null)
                .collect(Collectors.toList());
    }

    public void removeFavorite(UUID userId, UUID placeId) {
        Optional<Favorite> favorite = favoriteMapper.findByUserAndPlace(userId, placeId);
        if (favorite.isEmpty()) {
            throw new RuntimeException("Favorite not found");
        }

        favoriteMapper.deleteByUserAndPlace(userId, placeId);
    }

    public boolean isFavorite(UUID userId, UUID placeId) {
        return favoriteMapper.findByUserAndPlace(userId, placeId).isPresent();
    }

    private FavoriteResponse mapToResponse(Favorite favorite, Place place) {
        return FavoriteResponse.builder()
                .favId(favorite.getFavId())
                .userId(favorite.getUserId())
                .placeId(favorite.getPlaceId())
                .placeName(place.getPlaceName())
                .mainImage(place.getMainImage())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
