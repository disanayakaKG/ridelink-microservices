package com.ridelink.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink – Fare & Payment Service")
                        .description("APIs for fare estimation, simulated payments, payment history and receipts.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RideLink Team")
                                .email("ridelink@example.com")));
    }
}
