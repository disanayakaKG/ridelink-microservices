package com.ridelink.driver.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// Service responsible for validating JWT tokens and extracting user claims
@Service
public class JwtService {

    private final SecretKey signingKey;

    // Initializes the cryptographic signing key using the application secret
    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Verifies the token signature and returns the payload claims
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extracts the user account ID (subject) from the token
    public String extractAccountId(String token) {
        return parseClaims(token).getSubject();
    }

    // Extracts the user role claim from the token
    public String extractRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }
}
