package com.example.tour_place_api.model.entity;

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
public class Favorite {
    private UUID favId;
    private UUID userId;
    private UUID placeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
