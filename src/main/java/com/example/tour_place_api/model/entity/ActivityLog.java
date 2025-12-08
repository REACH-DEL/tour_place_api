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
public class ActivityLog {
    private UUID activityId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String entityName;
    private UUID userId;
    private String userEmail; // Populated by mapper join
    private LocalDateTime createdAt;
}

