package com.example.tour_place_api.service;

import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private final ConcurrentHashMap<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRATION_MINUTES = 2;

    public String generateOtp(String email) {
        String otp = generateRandomOtp();
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES);
        otpStorage.put(email, new OtpData(otp, expirationTime, null, null));
        return otp;
    }

    public String generateOtpWithRegistrationData(String email, String fullName, String password) {
        String otp = generateRandomOtp();
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES);
        otpStorage.put(email, new OtpData(otp, expirationTime, fullName, password));
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            return false;
        }

        if (System.currentTimeMillis() > otpData.expirationTime) {
            otpStorage.remove(email);
            return false;
        }

        if (otpData.otp.equals(otp)) {
            otpStorage.remove(email);
            return true;
        }

        return false;
    }

    public OtpData getOtpData(String email) {
        OtpData otpData = otpStorage.get(email);
        if (otpData != null && System.currentTimeMillis() > otpData.expirationTime) {
            otpStorage.remove(email);
            return null;
        }
        return otpData;
    }

    public void removeOtp(String email) {
        otpStorage.remove(email);
    }

    private String generateRandomOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public static class OtpData {
        public String otp;
        public long expirationTime;
        public String fullName;
        public String password;

        public OtpData(String otp, long expirationTime, String fullName, String password) {
            this.otp = otp;
            this.expirationTime = expirationTime;
            this.fullName = fullName;
            this.password = password;
        }
    }
}
