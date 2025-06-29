package com.shrinkerkit.shrinker_kit_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configures the application's security settings, including CORS.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS using the custom configuration source below
            .cors(withDefaults())
            // Disable CSRF protection for our stateless API
            .csrf(csrf -> csrf.disable())
            // Define authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow public access to the URL shortening and redirection endpoints
                .requestMatchers("/api/v1/urls").permitAll()
                .requestMatchers("/*").permitAll()
                // Require authentication for any other request (optional for now)
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * Creates a CORS configuration source to allow requests from the frontend.
     * @return The configured CorsConfigurationSource.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from local dev servers and the deployed Vercel frontend
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000", 
            "http://localhost:3002",
            "https://shrinker-kit-frontend-hw90ph543-tshrrxs-projects.vercel.app" // FIX: Added your live Vercel URL
        ));
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        // Allow necessary headers
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}