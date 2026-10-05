package com.ridelink.ride.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Permissive security configuration for local development and demonstration.
 * Authentication/authorization is expected to be enforced by the Account Service
 * (JWT) in the fully integrated system. Swagger and all /api/** endpoints are
 * open so the service can be exercised independently via Postman/Swagger.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// Configures HTTP security rules, permitting Swagger and API endpoints for development and testing.
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Disable CSRF and configure stateless session policy for REST microservices
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Configure request authorization rules
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**",
								"/api-docs/**",
								"/actuator/**",
								"/error")
						.permitAll()
						.requestMatchers("/api/**").permitAll()
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults());
		return http.build();
	}
}
