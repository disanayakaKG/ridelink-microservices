package com.ridelink.ride.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;

/**
 * Configures signature verification compatible with Account Service HMAC key selection.
 * Validates issuer, timestamps with zero clock skew, expiration presence, a nonblank subject
 * and a supported role claim.
 */
@Configuration
public class JwtConfig {
    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret,
                                 @Value("${jwt.issuer}") String issuer) {
        if (secret.isBlank() || secret.startsWith("${")) {
            throw new IllegalArgumentException("JWT_SECRET must be configured");
        }
        // Match Account Service JwtService.key(): raw UTF-8, padded to 32 bytes,
        // then JJWT's key-length-based HMAC algorithm selection.
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = Arrays.copyOf(bytes, 32);
        }
        MacAlgorithm algorithm = bytes.length >= 64 ? MacAlgorithm.HS512
                : bytes.length >= 48 ? MacAlgorithm.HS384 : MacAlgorithm.HS256;
        String keyAlgorithm = "HmacSHA" + algorithm.getName().substring(2);
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(bytes, keyAlgorithm))
                .macAlgorithm(algorithm).build();

        OAuth2TokenValidator<Jwt> requiredClaims = jwt -> {
            Object role = jwt.getClaims().get("role");
            if (jwt.getExpiresAt() == null || jwt.getSubject() == null
                    || jwt.getSubject().isBlank() || !(role instanceof String)
                    || !Set.of("PASSENGER", "DRIVER", "ADMIN").contains(role)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Missing or invalid Account Service claims", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO),
                new JwtIssuerValidator(issuer),
                requiredClaims));
        return decoder;
    }
}
