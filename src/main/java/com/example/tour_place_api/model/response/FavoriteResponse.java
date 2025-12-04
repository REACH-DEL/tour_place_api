package com.example.tour_place_api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {
    private UUID favId;
    private UUID userId;
    private UUID placeId;
    private String placeName;
    private String mainImage;
    private LocalDateTime createdAt;
}
