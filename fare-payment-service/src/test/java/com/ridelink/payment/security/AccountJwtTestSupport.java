package com.ridelink.payment.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public abstract class AccountJwtTestSupport {
    protected static final String TEST_SECRET = randomSecret(32);

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> TEST_SECRET);
        registry.add("jwt.issuer", () -> "ridelink-account");
        // Never connect tests to the developer's configured MongoDB server.
        registry.add("spring.mongodb.uri", () -> "mongodb://localhost:27017/ridelink_payment_test");
        registry.add("spring.mongodb.database", () -> "ridelink_payment_test");
    }

    protected static String randomSecret(int bytes) {
        byte[] value = new byte[bytes];
        new SecureRandom().nextBytes(value);
        return Base64.getEncoder().encodeToString(value);
    }

    // Reproduce origin/feature/account-core JwtService.createToken/key using
    // the same JJWT version. Signing exists only in test sources.
    protected static String accountToken(String secret, String role, Instant expiration, String issuer) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = Arrays.copyOf(bytes, 32);
        }
        return Jwts.builder().subject("PASS001").claim("role", role)
                .claim("email", "passenger@example.test").issuer(issuer)
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(expiration == null ? null : Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(bytes)).compact();
    }

    protected static String validToken(String role) {
        return accountToken(TEST_SECRET, role, Instant.now().plusSeconds(600), "ridelink-account");
    }
}
