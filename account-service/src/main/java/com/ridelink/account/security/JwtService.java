package com.ridelink.account.security;

import com.ridelink.account.config.JwtProperties;
import com.ridelink.account.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Issues signed account JWTs containing subject, role and email claims. Parsing verifies the
 * signature and token time constraints; it does not require a particular issuer.
 */
@Service
public class JwtService {

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    /**
     * Signs account identity claims with the configured issuer and expiration.
     */
    public String createToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpirationMs());
        return Jwts.builder()
                .subject(user.getId())
                .claim("role", user.getRole())
                .claim("email", user.getEmail())
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    /**
     * Verifies signed claims and token time constraints without an issuer-equality requirement.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String userIdFrom(String token) {
        return parse(token).getSubject();
    }

    public String roleFrom(String token) {
        Object role = parse(token).get("role");
        return role == null ? null : role.toString();
    }

    private SecretKey key() {
        byte[] bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = java.util.Arrays.copyOf(bytes, 32);
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}