package com.example.tour_place_api.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileImageRequest {
    @NotBlank(message = "Profile image URL is required")
    private String profileImageUrl;
}

