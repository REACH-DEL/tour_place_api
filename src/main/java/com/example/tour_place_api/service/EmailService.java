package com.example.tour_place_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Tour Place API - OTP Verification");
        message.setText("Your OTP for registration is: " + otp + "\n\nThis OTP is valid for 2 minutes.");
        
        // Use configured email or default
        if (fromEmail != null && !fromEmail.isEmpty() && !fromEmail.equals("your-email@gmail.com")) {
            message.setFrom(fromEmail);
        } else {
            message.setFrom("noreply@tourplace.com");
        }

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String email, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Welcome to Tour Place API");
        message.setText("Hello " + fullName + ",\n\nWelcome to Tour Place API! Your account has been successfully created.\n\nHappy exploring!");
        
        // Use configured email or default
        if (fromEmail != null && !fromEmail.isEmpty() && !fromEmail.equals("your-email@gmail.com")) {
            message.setFrom(fromEmail);
        } else {
            message.setFrom("noreply@tourplace.com");
        }

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
