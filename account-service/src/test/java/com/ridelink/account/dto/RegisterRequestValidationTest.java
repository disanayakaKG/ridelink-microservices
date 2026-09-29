package com.ridelink.account.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequestPasses() {
        RegisterRequest req = base();
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void badEmailFails() {
        RegisterRequest req = base();
        req.setEmail("not-an-email");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void shortPasswordFails() {
        RegisterRequest req = base();
        req.setPassword("short");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void invalidRoleFails() {
        RegisterRequest req = base();
        req.setRole("STUDENT");
        assertFalse(validator.validate(req).isEmpty());
    }

    private RegisterRequest base() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("passenger1@ridelink.lk");
        req.setPassword("Passw0rd!");
        req.setFullName("Test Passenger");
        req.setRole("PASSENGER");
        return req;
    }
}