package com.example.tour_place_api.service;

import com.example.tour_place_api.model.entity.ActivityLog;
import com.example.tour_place_api.model.response.DashboardStatsResponse;
import com.example.tour_place_api.model.response.PlacesOverviewResponse;
import com.example.tour_place_api.model.response.RecentActivityResponse;
import com.example.tour_place_api.repository.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    public DashboardStatsResponse getDashboardStats() {
        // Get current counts
        Long totalUsers = dashboardMapper.countTotalUsers();
        Long totalPlaces = dashboardMapper.countTotalPlaces();
        Long totalImages = dashboardMapper.countTotalImages();

        // Get previous month counts for percentage calculation
        Long usersPreviousMonth = dashboardMapper.countUsersPreviousMonth();
        Long placesPreviousMonth = dashboardMapper.countPlacesPreviousMonth();
        Long imagesPreviousMonth = dashboardMapper.countImagesPreviousMonth();

        // Calculate percentage changes
        Double usersChange = calculatePercentageChange(totalUsers, usersPreviousMonth);
        Double placesChange = calculatePercentageChange(totalPlaces, placesPreviousMonth);
        Double imagesChange = calculatePercentageChange(totalImages, imagesPreviousMonth);

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalUsersChange(usersChange)
                .totalPlaces(totalPlaces)
                .totalPlacesChange(placesChange)
                .totalImages(totalImages)
                .totalImagesChange(imagesChange)
                .build();
    }

    public List<PlacesOverviewResponse> getPlacesOverview(int months) {
        // Validate months parameter
        if (months < 1) months = 1;
        if (months > 12) months = 12;

        return dashboardMapper.getPlacesOverviewByMonths(months);
    }

    public List<RecentActivityResponse> getRecentActivity(int limit, int offset) {
        // Validate parameters
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<ActivityLog> activities = dashboardMapper.findRecentActivities(limit, offset);

        return activities.stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());
    }

    // Helper method to log activities (can be called from other services)
    public void logActivity(String action, String entityType, UUID entityId, String entityName, UUID userId) {
        ActivityLog activityLog = ActivityLog.builder()
                .activityId(UUID.randomUUID())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        dashboardMapper.insertActivity(activityLog);
    }

    private Double calculatePercentageChange(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }

    private RecentActivityResponse mapToActivityResponse(ActivityLog activityLog) {
        String actionLabel = getActionLabel(activityLog.getAction());
        String timeAgo = calculateTimeAgo(activityLog.getCreatedAt());

        return RecentActivityResponse.builder()
                .id(activityLog.getActivityId())
                .action(activityLog.getAction())
                .actionLabel(actionLabel)
                .entityType(activityLog.getEntityType())
                .entityId(activityLog.getEntityId())
                .entityName(activityLog.getEntityName())
                .userId(activityLog.getUserId())
                .userEmail(activityLog.getUserEmail())
                .timestamp(activityLog.getCreatedAt())
                .timeAgo(timeAgo)
                .build();
    }

    private String getActionLabel(String action) {
        return switch (action) {
            case "PLACE_CREATED" -> "New place added";
            case "PLACE_UPDATED" -> "Place updated";
            case "PLACE_DELETED" -> "Place deleted";
            case "IMAGE_UPLOADED" -> "Image uploaded";
            case "IMAGE_DELETED" -> "Image deleted";
            case "USER_REGISTERED" -> "New user registered";
            case "USER_ENABLED" -> "User enabled";
            case "USER_DISABLED" -> "User disabled";
            case "USER_ROLE_UPDATED" -> "User role updated";
            default -> action;
        };
    }

    private String calculateTimeAgo(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "Unknown";
        }

        Duration duration = Duration.between(timestamp, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        }
    }
}

