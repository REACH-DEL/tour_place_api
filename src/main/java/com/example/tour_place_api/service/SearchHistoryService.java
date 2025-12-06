package com.example.tour_place_api.service;

import com.example.tour_place_api.model.entity.Place;
import com.example.tour_place_api.model.entity.SearchHistory;
import com.example.tour_place_api.model.request.CreateSearchHistoryRequest;
import com.example.tour_place_api.model.response.SearchHistoryResponse;
import com.example.tour_place_api.repository.mapper.PlaceMapper;
import com.example.tour_place_api.repository.mapper.SearchHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SearchHistoryService {
    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Autowired
    private PlaceMapper placeMapper;

    public SearchHistoryResponse createOrUpdateSearchHistory(UUID userId, CreateSearchHistoryRequest request) {
        UUID placeId = request.getPlaceId();
        
        // Verify place exists
        Optional<Place> placeOptional = placeMapper.findById(placeId);
        if (placeOptional.isEmpty()) {
            throw new RuntimeException("Place not found");
        }

        // Check if search history already exists for this user and place
        Optional<SearchHistory> existingHistory = searchHistoryMapper.findByUserAndPlace(userId, placeId);
        
        if (existingHistory.isPresent()) {
            // Update the timestamp to make it the latest search
            searchHistoryMapper.updateTimestampByUserAndPlace(userId, placeId);
            SearchHistory updated = existingHistory.get();
            // Refresh to get updated timestamp
            updated = searchHistoryMapper.findByUserAndPlace(userId, placeId).orElse(updated);
            Place place = placeOptional.get();
            return mapToResponse(updated, place);
        } else {
            // Create new search history
            SearchHistory searchHistory = SearchHistory.builder()
                    .searchId(UUID.randomUUID())
                    .userId(userId)
                    .placeId(placeId)
                    .build();
            
            searchHistoryMapper.insert(searchHistory);
            
            // Fetch the created history to get timestamps
            SearchHistory created = searchHistoryMapper.findByUserAndPlace(userId, placeId)
                    .orElse(searchHistory);
            Place place = placeOptional.get();
            return mapToResponse(created, place);
        }
    }

    public List<SearchHistoryResponse> getLatestSearchHistory(UUID userId) {
        List<SearchHistory> histories = searchHistoryMapper.findLatestByUserId(userId);
        
        return histories.stream()
                .map(history -> {
                    Optional<Place> placeOptional = placeMapper.findById(history.getPlaceId());
                    return placeOptional.map(place -> mapToResponse(history, place)).orElse(null);
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    public void updateSearchHistoryTimestamp(UUID searchId, UUID userId) {
        Optional<SearchHistory> historyOptional = searchHistoryMapper.findById(searchId);
        
        if (historyOptional.isEmpty()) {
            throw new RuntimeException("Search history not found");
        }
        
        SearchHistory history = historyOptional.get();
        if (!history.getUserId().equals(userId)) {
            throw new RuntimeException("You can only update your own search history");
        }
        
        searchHistoryMapper.updateTimestamp(searchId);
    }

    public void deleteSearchHistory(UUID searchId, UUID userId) {
        Optional<SearchHistory> historyOptional = searchHistoryMapper.findById(searchId);
        
        if (historyOptional.isEmpty()) {
            throw new RuntimeException("Search history not found");
        }
        
        SearchHistory history = historyOptional.get();
        if (!history.getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own search history");
        }
        
        searchHistoryMapper.delete(searchId);
    }

    private SearchHistoryResponse mapToResponse(SearchHistory history, Place place) {
        return SearchHistoryResponse.builder()
                .searchId(history.getSearchId())
                .userId(history.getUserId())
                .placeId(history.getPlaceId())
                .placeName(place.getPlaceName())
                .mainImage(place.getMainImage())
                .createdAt(history.getCreatedAt())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}

