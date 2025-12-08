package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.DashboardStatsResponse;
import com.example.tour_place_api.model.response.PlacesOverviewResponse;
import com.example.tour_place_api.model.response.RecentActivityResponse;
import com.example.tour_place_api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get dashboard statistics",
               description = "Returns statistics for the dashboard including total counts and percentage changes. (Requires ROLE_ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        try {
            DashboardStatsResponse stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(
                    ApiResponse.<DashboardStatsResponse>builder()
                            .success(true)
                            .message("Statistics retrieved successfully")
                            .payload(stats)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<DashboardStatsResponse>builder()
                            .success(false)
                            .message("Error retrieving statistics: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Get places overview chart data",
               description = "Returns monthly data for places creation chart. (Requires ROLE_ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/places-overview")
    public ResponseEntity<ApiResponse<List<PlacesOverviewResponse>>> getPlacesOverview(
            @Parameter(description = "Number of months to retrieve data for (1-12, default: 6)", required = false)
            @RequestParam(value = "months", required = false, defaultValue = "6") int months) {
        try {
            List<PlacesOverviewResponse> overview = dashboardService.getPlacesOverview(months);
            return ResponseEntity.ok(
                    ApiResponse.<List<PlacesOverviewResponse>>builder()
                            .success(true)
                            .message("Places overview retrieved successfully")
                            .payload(overview)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<PlacesOverviewResponse>>builder()
                            .success(false)
                            .message("Error retrieving places overview: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Get recent activity",
               description = "Returns list of recent activities/actions performed in the system. (Requires ROLE_ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse<List<RecentActivityResponse>>> getRecentActivity(
            @Parameter(description = "Number of activities to return (1-100, default: 10)", required = false)
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @Parameter(description = "Pagination offset (default: 0)", required = false)
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset) {
        try {
            List<RecentActivityResponse> activities = dashboardService.getRecentActivity(limit, offset);
            return ResponseEntity.ok(
                    ApiResponse.<List<RecentActivityResponse>>builder()
                            .success(true)
                            .message("Recent activity retrieved successfully")
                            .payload(activities)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<RecentActivityResponse>>builder()
                            .success(false)
                            .message("Error retrieving recent activity: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }
}

