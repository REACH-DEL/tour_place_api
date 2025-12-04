package com.example.tour_place_api.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddImagesRequest {
    @NotEmpty(message = "Image URLs list cannot be empty")
    private List<String> imageUrls;
}

