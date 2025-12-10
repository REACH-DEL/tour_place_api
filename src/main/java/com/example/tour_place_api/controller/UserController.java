package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.request.*;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.LoginResponse;
import com.example.tour_place_api.model.response.OtpStatusResponse;
import com.example.tour_place_api.model.response.UserResponse;
import com.example.tour_place_api.service.AuthService;
import com.example.tour_place_api.service.DashboardService;
import com.example.tour_place_api.service.OtpService;
import com.example.tour_place_api.security.JwtAuthenticationDetails;
import com.example.tour_place_api.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private OtpService otpService;

    @Operation(summary = "Register user", description = "Register a new user and send OTP to email", security = {})
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.<Void>builder()
                            .success(true)
                            .message("OTP sent to email. Please verify OTP to complete registration.")
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Verify OTP", description = "Verify OTP and complete user registration", security = {})
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        try {
            String token = authService.verifyOtpAndCreateUser(
                    request.getEmail(), 
                    request.getOtp()
            );

            // Get user info from token
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
            UserResponse user = authService.getUserById(userId);

            // Log activity
            dashboardService.logActivity("USER_REGISTERED", "USER", userId, user.getEmail(), userId);

            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .user(user)
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.<LoginResponse>builder()
                            .success(true)
                            .message("User registered and verified successfully")
                            .payload(loginResponse)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Resend OTP", description = "Resend OTP to user email", security = {})
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.resendOtp(request.getEmail(), request.getFullName(), request.getPassword());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.<Void>builder()
                            .success(true)
                            .message("OTP sent to email. Please verify OTP to complete registration.")
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Login", description = "Login user and get JWT token", security = {})
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.loginUser(request);

            // Get user info from token
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
            UserResponse user = authService.getUserById(userId);

            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .user(user)
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.<LoginResponse>builder()
                            .success(true)
                            .message("Login successful")
                            .payload(loginResponse)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<LoginResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }
    }

    @Operation(summary = "Get user profile", description = "Get current authenticated user profile", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            UserResponse user = authService.getUserById(userId);
            return ResponseEntity.ok(
                    ApiResponse.<UserResponse>builder()
                            .success(true)
                            .message("User profile retrieved successfully")
                            .payload(user)
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.UNAUTHORIZED)
                            .build());
        }
    }

    @Operation(summary = "Logout", description = "Logout the current user. Client should remove the token from storage. (Requires authentication)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        try {
            // Clear the security context (though it's stateless, this ensures cleanup)
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Logout successful. Please remove the token from client storage.")
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Error during logout: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }

    @Operation(summary = "Update profile image", description = "Update the profile image URL for the current authenticated user", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/profile/image")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfileImage(
            @Valid @RequestBody UpdateProfileImageRequest request,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            UserResponse user = authService.updateProfileImage(userId, request.getProfileImageUrl());
            return ResponseEntity.ok(
                    ApiResponse.<UserResponse>builder()
                            .success(true)
                            .message("Profile image updated successfully")
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

    @Operation(summary = "Change password", description = "Change password for the current authenticated user using old password confirmation", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/profile/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
            UUID userId = UUID.fromString(details.getUserId());

            authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Password changed successfully")
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Forgot password", description = "Request password reset OTP to be sent to email", security = {})
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Password reset OTP sent to email. Please check your inbox.")
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Reset password", description = "Reset password using OTP verification", security = {})
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Password reset successfully. Please login with your new password.")
                            .status(HttpStatus.OK)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @Operation(summary = "Get OTP status", description = "Get OTP countdown status for password reset", security = {})
    @PostMapping("/otp-status")
    public ResponseEntity<ApiResponse<OtpStatusResponse>> getOtpStatus(
            @Valid @RequestBody OtpStatusRequest request) {
        try {
            long remainingSeconds = otpService.getRemainingSeconds(request.getEmail());
            boolean hasOtp = remainingSeconds > 0;

            OtpStatusResponse response = OtpStatusResponse.builder()
                    .hasOtp(hasOtp)
                    .remainingSeconds(remainingSeconds)
                    .message(hasOtp 
                            ? "OTP is valid. " + remainingSeconds + " seconds remaining."
                            : "No active OTP found or OTP has expired.")
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.<OtpStatusResponse>builder()
                            .success(true)
                            .message("OTP status retrieved successfully")
                            .payload(response)
                            .status(HttpStatus.OK)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<OtpStatusResponse>builder()
                            .success(false)
                            .message("Error retrieving OTP status: " + e.getMessage())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
        }
    }
}
