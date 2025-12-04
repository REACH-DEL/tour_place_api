package com.example.tour_place_api.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePlaceRequest {
    @Size(min = 3, max = 255, message = "Place name must be between 3 and 255 characters")
    private String placeName;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @DecimalMin(value = "-90.0000000", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0000000", message = "Latitude must be between -90 and 90")
    private BigDecimal lat;

    @DecimalMin(value = "-180.0000000", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0000000", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String mainImageUrl;
}
