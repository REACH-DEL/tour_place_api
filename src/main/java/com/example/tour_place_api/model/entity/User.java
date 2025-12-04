package com.example.tour_place_api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID userId;
    private String fullName;
    private String email;
    private String password;
    private Boolean status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
