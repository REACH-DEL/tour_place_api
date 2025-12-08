package com.example.tour_place_api.service;

import com.example.tour_place_api.model.entity.User;
import com.example.tour_place_api.model.request.UpdateUserRoleRequest;
import com.example.tour_place_api.model.request.UpdateUserStatusRequest;
import com.example.tour_place_api.model.response.UserResponse;
import com.example.tour_place_api.repository.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DashboardService dashboardService;

    public List<UserResponse> getAllUsers() {
        // Get only regular users (exclude admin role)
        List<User> users = userMapper.findAllRegularUsers();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(UUID userId) {
        Optional<User> userOptional = userMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        return mapToResponse(userOptional.get());
    }

    public UserResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request, UUID currentAdminId) {
        Optional<User> userOptional = userMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();

        // Prevent admin from disabling themselves
        if (userId.equals(currentAdminId) && !request.getStatus()) {
            throw new RuntimeException("You cannot disable your own account");
        }

        Optional<User> updatedUserOptional = userMapper.updateStatus(userId, request.getStatus());
        if (updatedUserOptional.isEmpty()) {
            throw new RuntimeException("Failed to update user status");
        }

        User updatedUser = updatedUserOptional.get();

        // Log activity
        String action = request.getStatus() ? "USER_ENABLED" : "USER_DISABLED";
        dashboardService.logActivity(action, "USER", userId, user.getEmail(), currentAdminId);

        return mapToResponse(updatedUser);
    }

    public UserResponse updateUserRole(UUID userId, UpdateUserRoleRequest request, UUID currentAdminId) {
        Optional<User> userOptional = userMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Validate role
        if (!request.getRole().equalsIgnoreCase("admin") && !request.getRole().equalsIgnoreCase("user")) {
            throw new RuntimeException("Invalid role. Role must be 'admin' or 'user'");
        }

        // Prevent admin from changing their own role
        if (userId.equals(currentAdminId)) {
            throw new RuntimeException("You cannot change your own role");
        }

        Optional<User> updatedUserOptional = userMapper.updateRole(userId, request.getRole().toLowerCase());
        if (updatedUserOptional.isEmpty()) {
            throw new RuntimeException("Failed to update user role");
        }

        User updatedUser = updatedUserOptional.get();

        // Log activity
        dashboardService.logActivity("USER_ROLE_UPDATED", "USER", userId, updatedUser.getEmail(), currentAdminId);

        return mapToResponse(updatedUser);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

