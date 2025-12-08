package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.request.UpdateUserRoleRequest;
import com.example.tour_place_api.model.request.UpdateUserStatusRequest;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.UserResponse;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import com.example.tour_place_api.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @Operation(summary = "Get all users",
               description = "Returns a list of all users in the system. (Requires ROLE_ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            List<UserResponse> users = userManagementService.getAllUsers();
            return ResponseEntity.ok(
                    ApiResponse.<List<UserResponse>>builder()
                            .success(true)
                            .message("Users retrieved successfully")
                            .payload(users)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<UserResponse>>builder()
                            .success(false)
                            .message("Error retrieving users: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Get user by ID",
               description = "Returns details of a specific user. (Requires ROLE_ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        try {
            UserResponse user = userManagementService.getUserById(userId);
            return ResponseEntity.ok(
                    ApiResponse.<UserResponse>builder()
                            .success(true)
                            .message("User retrieved successfully")
                            .payload(user)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        }
    }

    @Operation(summary = "Update user status",
               description = "Enables or disables a user account. Admins cannot disable themselves. (Requires ROLE_ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID currentAdminId = UUID.fromString(details.getUserId());

            UserResponse user = userManagementService.updateUserStatus(userId, request, currentAdminId);
            return ResponseEntity.ok(
                    ApiResponse.<UserResponse>builder()
                            .success(true)
                            .message("User status updated successfully")
                            .payload(user)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Update user role",
               description = "Updates a user's role. Admins cannot change their own role. (Requires ROLE_ADMIN)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID currentAdminId = UUID.fromString(details.getUserId());

            UserResponse user = userManagementService.updateUserRole(userId, request, currentAdminId);
            return ResponseEntity.ok(
                    ApiResponse.<UserResponse>builder()
                            .success(true)
                            .message("User role updated successfully")
                            .payload(user)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }
}

