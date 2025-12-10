package com.example.tour_place_api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceRatingResponse {
    private java.util.UUID placeId;
    private String placeName;
    private Double averageRating;
    private Long totalReviews;
    private Long ratingCount1;
    private Long ratingCount2;
    private Long ratingCount3;
    private Long ratingCount4;
    private Long ratingCount5;
}

