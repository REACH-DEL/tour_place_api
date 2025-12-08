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
public class RecentActivityResponse {
    private UUID id;
    private String action;
    private String actionLabel;
    private String entityType;
    private UUID entityId;
    private String entityName;
    private UUID userId;
    private String userEmail;
    private LocalDateTime timestamp;
    private String timeAgo;
}

