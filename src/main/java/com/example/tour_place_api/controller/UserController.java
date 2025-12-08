package com.example.tour_place_api.controller;

import com.example.tour_place_api.model.request.RegisterRequest;
import com.example.tour_place_api.model.request.VerifyOtpRequest;
import com.example.tour_place_api.model.request.LoginRequest;
import com.example.tour_place_api.model.response.ApiResponse;
import com.example.tour_place_api.model.response.LoginResponse;
import com.example.tour_place_api.model.response.UserResponse;
import com.example.tour_place_api.service.AuthService;
import com.example.tour_place_api.service.DashboardService;
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
}
