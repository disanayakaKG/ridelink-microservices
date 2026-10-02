package com.ridelink.ride.security;

import com.ridelink.ride.config.JwtConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtContractTest extends AccountJwtTestSupport {
    @ParameterizedTest
    @CsvSource({"12,HS256", "32,HS256", "40,HS384", "48,HS512"})
    void matchesAccountKeyPaddingAndAlgorithmSelection(int randomBytes, String expectedAlgorithm) {
        String secret = randomSecret(randomBytes);
        var decoder = new JwtConfig().jwtDecoder(secret, "ridelink-account");
        var jwt = decoder.decode(accountToken(secret, "PASSENGER", Instant.now().plusSeconds(600), "ridelink-account"));
        assertEquals(expectedAlgorithm, jwt.getHeaders().get("alg"));
        assertEquals("PASS001", jwt.getSubject());
        assertEquals("PASSENGER", jwt.getClaimAsString("role"));
    }

    @Test
    void rejectsUnsignedToken() {
        var decoder = new JwtConfig().jwtDecoder(TEST_SECRET, "ridelink-account");
        String unsigned = io.jsonwebtoken.Jwts.builder().subject("PASS001")
                .claim("role", "ADMIN").compact();
        assertThrows(JwtException.class, () -> decoder.decode(unsigned));
    }

    @Test
    void rejectsMissingSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtConfig().jwtDecoder("", "ridelink-account"));
        assertThrows(IllegalArgumentException.class, () -> new JwtConfig().jwtDecoder("${JWT_SECRET}", "ridelink-account"));
    }
}
