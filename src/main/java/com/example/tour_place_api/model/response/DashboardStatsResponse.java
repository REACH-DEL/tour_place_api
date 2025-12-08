package com.example.tour_place_api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private Long totalUsers;
    private Double totalUsersChange;
    private Long totalPlaces;
    private Double totalPlacesChange;
    private Long totalImages;
    private Double totalImagesChange;
}

