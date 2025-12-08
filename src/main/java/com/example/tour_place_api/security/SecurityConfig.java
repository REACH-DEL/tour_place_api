package com.example.tour_place_api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Allow Swagger UI and OpenAPI endpoints (paths after context-path is stripped)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/index.html").permitAll()
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/verify-otp", "/api/v1/auth/resend-otp", "/api/v1/auth/login").permitAll()
                // Public place endpoints (GET only)
                .requestMatchers(HttpMethod.GET, "/api/v1/places/**").permitAll()
                // Require ROLE_ADMIN for POST, PUT, DELETE place endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/places").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/places/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/places/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/places/*/images").hasRole("ADMIN")
                // Public file view endpoint
                .requestMatchers(HttpMethod.GET, "/api/v1/file/view").permitAll()
                // File upload and delete endpoints require authentication (permissions checked in controller)
                .requestMatchers(HttpMethod.POST, "/api/v1/file/upload").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/file/delete/**").authenticated()
                // Require ROLE_USER for favorites endpoints
                .requestMatchers("/api/v1/favorites/**").hasRole("USER")
                // Require authentication for search history endpoints
                .requestMatchers("/api/v1/search-history/**").authenticated()
                // Require authentication for profile endpoint
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/profile").authenticated()
                // Require ROLE_ADMIN for dashboard endpoints
                .requestMatchers("/api/v1/dashboard/**").hasRole("ADMIN")
                // Require ROLE_ADMIN for user management endpoints
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                // All other /api/v1/** endpoints require authentication
                .requestMatchers("/api/v1/**").authenticated()
                // Allow all other requests (like error pages, etc.)
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
