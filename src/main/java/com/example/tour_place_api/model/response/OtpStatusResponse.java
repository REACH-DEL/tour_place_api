package com.example.tour_place_api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpStatusResponse {
    private boolean hasOtp;
    private long remainingSeconds;
    private String message;
}

