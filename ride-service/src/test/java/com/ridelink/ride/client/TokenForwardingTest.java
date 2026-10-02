package com.ridelink.ride.client;

import com.ridelink.ride.config.RestClientConfig;
import com.ridelink.ride.config.JwtConfig;
import com.ridelink.ride.security.AccountJwtTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class TokenForwardingTest extends AccountJwtTestSupport {
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private String authenticate(String role) {
        String token = validToken(role);
        var jwt = new JwtConfig().jwtDecoder(TEST_SECRET, "ridelink-account").decode(token);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
        return token;
    }

    @Test
    void driverCallsForwardCurrentTokenWithoutRetainingPreviousCaller() {
        var builder = new RestClientConfig().restClientBuilder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new DriverServiceClient("http://driver.test", builder);
        String passenger = authenticate("PASSENGER");
        server.expect(requestTo("http://driver.test/api/drivers/available"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + passenger))
                .andRespond(withSuccess("[{\"id\":\"driver-1\"}]", MediaType.APPLICATION_JSON));
        assertEquals(java.util.List.of("driver-1"), client.getAvailableDriverIds());
        server.verify();
        server.reset();

        String driver = authenticate("DRIVER");
        server.expect(requestTo("http://driver.test/api/drivers/available"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + driver))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        client.getAvailableDriverIds();
        server.verify();
        server.reset();

        SecurityContextHolder.clearContext();
        server.expect(requestTo("http://driver.test/api/drivers/available"))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        client.getAvailableDriverIds();
        server.verify();
    }

    @Test
    void paymentCallsForwardSameAccountToken() {
        var builder = new RestClientConfig().restClientBuilder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new PaymentServiceClient("http://payment.test", builder);
        String token = authenticate("PASSENGER");
        server.expect(requestTo("http://payment.test/api/fares/estimate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(content().json("{\"distanceKm\":10}"))
                .andRespond(withSuccess("{\"estimatedFare\":950}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://payment.test/api/payments"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(content().json("{\"rideId\":\"RIDE001\",\"passengerId\":\"PASS001\",\"amount\":950,\"paymentMethod\":\"CARD\"}"))
                .andRespond(withSuccess("{\"id\":\"payment-1\"}", MediaType.APPLICATION_JSON));
        assertEquals(950, client.estimateFare(10));
        assertEquals("payment-1", client.createPayment("RIDE001", "PASS001", 950));
        server.verify();
    }
}
