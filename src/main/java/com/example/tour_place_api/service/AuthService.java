package com.example.tour_place_api.service;

import com.example.tour_place_api.model.entity.User;
import com.example.tour_place_api.model.request.RegisterRequest;
import com.example.tour_place_api.model.request.LoginRequest;
import com.example.tour_place_api.model.response.UserResponse;
import com.example.tour_place_api.repository.mapper.UserMapper;
import com.example.tour_place_api.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(RegisterRequest request) {
        if (userMapper.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        String otp = otpService.generateOtpWithRegistrationData(
            request.getEmail(), 
            request.getFullName(), 
            request.getPassword()
        );
        emailService.sendOtpEmail(request.getEmail(), otp);
    }

    public String verifyOtpAndCreateUser(String email, String otp) {
        OtpService.OtpData otpData = otpService.getOtpData(email);
        
        if (otpData == null || !otpData.otp.equals(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        if (otpData.fullName == null || otpData.password == null) {
            throw new RuntimeException("Registration data not found. Please register again.");
        }

        if (!userMapper.existsByEmail(email)) {
            User user = User.builder()
                    .userId(UUID.randomUUID())
                    .fullName(otpData.fullName)
                    .email(email)
                    .password(passwordEncoder.encode(otpData.password))
                    .status(true)
                    .role("user")
                    .build();

            userMapper.insert(user);

            emailService.sendWelcomeEmail(email, otpData.fullName);
            
            // Remove OTP data after successful verification
            otpService.removeOtp(email);

            return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRole());
        }

        throw new RuntimeException("User already exists");
    }

    public void resendOtp(String email, String fullName, String password) {
        if (userMapper.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        String otp = otpService.generateOtpWithRegistrationData(email, fullName, password);
        emailService.sendOtpEmail(email, otp);
    }

    public String loginUser(LoginRequest request) {
        Optional<User> userOptional = userMapper.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!user.getStatus()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRole());
    }

    public UserResponse getUserById(UUID userId) {
        Optional<User> userOptional = userMapper.findById(userId);
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return mapToResponse(userOptional.get());
    }

    public UserResponse updateProfileImage(UUID userId, String profileImageUrl) {
        Optional<User> userOptional = userMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Optional<User> updatedUserOptional = userMapper.updateProfileImage(userId, profileImageUrl);
        if (updatedUserOptional.isEmpty()) {
            throw new RuntimeException("Failed to update profile image");
        }

        return mapToResponse(updatedUserOptional.get());
    }

    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        Optional<User> updatedUserOptional = userMapper.updatePassword(userId, encodedNewPassword);
        if (updatedUserOptional.isEmpty()) {
            throw new RuntimeException("Failed to update password");
        }
    }

    public void forgotPassword(String email) {
        Optional<User> userOptional = userMapper.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with this email");
        }

        User user = userOptional.get();
        if (!user.getStatus()) {
            throw new RuntimeException("User account is disabled");
        }

        String otp = otpService.generateOtp(email);
        emailService.sendPasswordResetOtpEmail(email, otp);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        Optional<User> userOptional = userMapper.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with this email");
        }

        User user = userOptional.get();
        if (!user.getStatus()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!otpService.verifyOtp(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        Optional<User> updatedUserOptional = userMapper.updatePassword(user.getUserId(), encodedNewPassword);
        if (updatedUserOptional.isEmpty()) {
            throw new RuntimeException("Failed to reset password");
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .role(user.getRole())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
