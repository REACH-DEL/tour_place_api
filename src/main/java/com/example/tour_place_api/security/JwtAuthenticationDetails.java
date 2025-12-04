package com.example.tour_place_api.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtAuthenticationDetails {
    private String userId;
    private String email;
    private String role;
}
