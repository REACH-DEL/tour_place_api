package com.example.tour_place_api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponse {
    private UUID placeId;
    private String placeName;
    private String description;
    private String mainImage;
    private BigDecimal lat;
    private BigDecimal longitude;
    private List<String> additionalImages;
    private Boolean isFavorite; // null if not authenticated, true/false if authenticated
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
