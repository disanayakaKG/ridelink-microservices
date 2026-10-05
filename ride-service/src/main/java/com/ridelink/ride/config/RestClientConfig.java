package com.ridelink.ride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

/**
 * Configures synchronous REST clients for Driver and Payment communication. Each call resolves
 * the authenticated JWT from the security context and forwards its Bearer token so downstream
 * services perform their own security checks.
 */
@Configuration
public class RestClientConfig {
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestInterceptor((request, body, execution) -> {
            // Resolve per call: never retain a user's token on the shared client.
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwt && jwt.isAuthenticated()) {
                request.getHeaders().setBearerAuth(jwt.getToken().getTokenValue());
            }
            return execution.execute(request, body);
        });
    }
}