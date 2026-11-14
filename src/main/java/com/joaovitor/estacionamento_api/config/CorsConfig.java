package com.joaovitor.estacionamento_api.config;

import com.joaovitor.estacionamento_api.apikey.ApiKeyFilter;
import com.joaovitor.estacionamento_api.apikey.RateLimitFilter;
import com.joaovitor.estacionamento_api.idempotencia.IdempotenciaFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                ApiKeyFilter.HEADER_API_KEY,
                IdempotenciaFilter.HEADER_IDEMPOTENCIA,
                "X-API-Version"));
        config.setExposedHeaders(List.of(
                HttpHeaders.LOCATION,
                IdempotenciaFilter.HEADER_REPETIDA,
                RateLimitFilter.HEADER_LIMIT,
                RateLimitFilter.HEADER_REMAINING,
                RateLimitFilter.HEADER_RETRY_AFTER));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
