package com.ridelink.payment.security;

import com.ridelink.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

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
    @MockitoBean PaymentService payments;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /api/payments", "POST, /api/payments",
            "GET, /api/payments/payment-123", "GET, /api/payments/ride/RIDE001",
            "GET, /api/payments/passenger/PASS001", "GET, /api/payments/payment-123/receipt",
            "PATCH, /api/payments/payment-123/status", "POST, /api/payments/fare/calculate"
    })
    void paymentEndpointsRequireToken(String method, String path) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, org.hamcrest.Matchers.startsWith("Bearer")));
        verifyNoInteractions(payments);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-jwt", "a.b.c", "Bearer"})
    void malformedTokenReturns401(String token) throws Exception {
        unauthorized(token);
    }

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
    void invalidRoleReturns401(String role) throws Exception {
        unauthorized(validToken(role));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASSENGER", "DRIVER", "ADMIN"})
    void accountJwtAuthenticatesUserAndMapsRole(String role) throws Exception {
        when(payments.getAllPayments()).thenAnswer(invocation -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertEquals("PASS001", authentication.getName());
            assertEquals(List.of("ROLE_" + role), authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .filter(authority -> authority.startsWith("ROLE_")).toList());
            return List.of();
        });
        var result = mvc.perform(get("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken(role)))
                .andExpect(status().isOk()).andExpect(content().json("[]")).andReturn();
        assertNull(result.getRequest().getSession(false));
        mvc.perform(get("/api/payments")).andExpect(status().isUnauthorized());
        verify(payments, times(1)).getAllPayments();
    }

    @Test
    void publicFareEstimateWorksWithoutToken() throws Exception {
        mvc.perform(post("/api/fares/estimate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanceKm\":10}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estimatedFare").value(950.0));
    }

    @Test
    void validTokenAllowsPostWithoutCsrfAndPreservesValidation() throws Exception {
        mvc.perform(post("/api/payments").header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken("PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed for request"));
        verifyNoInteractions(payments);
        mvc.perform(post("/api/payments/fare/calculate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken("PASSENGER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"distanceKm\":10}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalFare").value(950));
    }

    @Test
    void openApiDocumentsPaymentEndpointsWithoutToken() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("RideLink – Fare & Payment Service"))
                .andExpect(jsonPath("$.paths['/api/fares/estimate'].post").exists())
                .andExpect(jsonPath("$.paths['/api/payments/fare/calculate'].post").exists())
                .andExpect(jsonPath("$.paths['/api/payments'].post").exists())
                .andExpect(jsonPath("$.paths['/api/payments'].get").exists())
                .andExpect(jsonPath("$.paths['/api/payments/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/payments/ride/{rideId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/payments/passenger/{passengerId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/payments/{id}/status'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/payments/{id}/receipt'].get").exists());
        verifyNoInteractions(payments);
    }

    @Test
    void swaggerUiAndConfigurationArePublic() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui/swagger-ui-bundle.js")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }

    @Test
    void basicLoginAndGeneratedPasswordAreDisabled() throws Exception {
        assertTrue(application.getBeansOfType(UserDetailsService.class).isEmpty());
        mvc.perform(get("/api/payments").header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
        mvc.perform(post("/login")).andExpect(status().isUnauthorized());
    }

    private void unauthorized(String token) throws Exception {
        mvc.perform(get("/api/payments").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(payments);
    }
}
