package com.ridelink.ride.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class JwtSecurityTest extends AccountJwtTestSupport {
    @Autowired WebApplicationContext context;
    @Autowired ApplicationContext application;
    @MockitoBean com.ridelink.ride.service.RideService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void noTokenReturns401() throws Exception {
        mvc.perform(get("/api/rides/passenger/PASS001")).andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, org.hamcrest.Matchers.startsWith("Bearer")));
        mvc.perform(post("/api/rides")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-jwt", "a.b.c", "Bearer"})
    void malformedTokenReturns401(String token) throws Exception { unauthorized(token); }

    @Test
    void expiredTokenReturns401() throws Exception {
        unauthorized(accountToken(TEST_SECRET, "PASSENGER", Instant.now().minusSeconds(5), "ridelink-account"));
    }

    @Test
    void wrongSignatureReturns401() throws Exception {
        unauthorized(accountToken(randomSecret(32), "PASSENGER", Instant.now().plusSeconds(600), "ridelink-account"));
    }

    @Test
    void wrongIssuerReturns401() throws Exception {
        unauthorized(accountToken(TEST_SECRET, "PASSENGER", Instant.now().plusSeconds(600), "another-service"));
    }

    @Test
    void missingExpirationReturns401() throws Exception {
        unauthorized(accountToken(TEST_SECRET, "PASSENGER", null, "ridelink-account"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_PASSENGER", "passenger", "", "UNKNOWN"})
    void invalidRoleReturns401(String role) throws Exception { unauthorized(validToken(role)); }

    @Test
    void missingAndInvalidRequiredClaimsReturn401() throws Exception {
        for (String subject : new String[] {null, "", " "}) {
            unauthorized(customClaimsToken(subject, "DRIVER", "ridelink-account"));
        }
        for (Object role : new Object[] {null, 42, List.of("ADMIN")}) {
            unauthorized(customClaimsToken("PASS001", role, "ridelink-account"));
        }
        unauthorized(customClaimsToken("PASS001", "DRIVER", null));
    }

    private String customClaimsToken(String subject, Object role, String issuer) {
        return io.jsonwebtoken.Jwts.builder().subject(subject).claim("role", role).issuer(issuer)
                .expiration(java.util.Date.from(Instant.now().plusSeconds(600)))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(TEST_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASSENGER", "DRIVER", "ADMIN"})
    void accountJwtAuthenticatesUserAndMapsRole(String role) throws Exception {
        when(service.getByPassengerId("PASS001")).thenAnswer(invocation -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertEquals("PASS001", authentication.getName());
            assertEquals(List.of("ROLE_" + role), authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority()).filter(a -> a.startsWith("ROLE_")).toList());
            return List.of();
        });
        var result = mvc.perform(get("/api/rides/passenger/PASS001").header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken(role)))
                .andExpect(status().isOk()).andExpect(content().json("[]")).andReturn();
        assertNull(result.getRequest().getSession(false));
        mvc.perform(get("/api/rides/passenger/PASS001")).andExpect(status().isUnauthorized());
        verify(service, times(1)).getByPassengerId("PASS001");
    }

    @Test
    void validPostPassesSecurityWithoutCsrf() throws Exception {
        mvc.perform(post("/api/rides").header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken("DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void swaggerRemainsPublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }

    @Test
    void basicLoginAndGeneratedPasswordAreDisabled() throws Exception {
        assertTrue(application.getBeansOfType(UserDetailsService.class).isEmpty());
        mvc.perform(get("/api/rides/passenger/PASS001").header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized()).andExpect(header().doesNotExist(HttpHeaders.LOCATION));
        mvc.perform(post("/login")).andExpect(status().isUnauthorized());
    }

    

    private void unauthorized(String token) throws Exception {
        mvc.perform(get("/api/rides/passenger/PASS001").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
}
