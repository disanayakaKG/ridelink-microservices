package com.ridelink.driver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Intercepts incoming HTTP requests once to validate JWT bearer tokens and populate the security context
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Helper service to decode and validate JWT tokens
    private final JwtService jwtService;

    // Filters incoming requests, extracts bearer tokens, and sets authentication in the SecurityContext
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String accountId = jwtService.extractAccountId(token);
                String role = jwtService.extractRole(token);

                var authorities = role != null
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        : List.<SimpleGrantedAuthority>of();

                var authentication = new UsernamePasswordAuthenticationToken(accountId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                // Invalid/expired token: leave unauthenticated, Spring Security will reject as needed
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}