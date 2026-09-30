package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exception.ApiException;
import com.ridelink.account.model.User;
import com.ridelink.account.repository.UserRepository;
import com.ridelink.account.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private AccountService service;

    private RegisterRequest validRegister;
    private LoginRequest validLogin;

    @BeforeEach
    void setUp() {
        validRegister = new RegisterRequest();
        validRegister.setEmail("passenger1@ridelink.lk");
        validRegister.setPassword("Passw0rd!");
        validRegister.setFullName("Test Passenger");
        validRegister.setPhone("0770000000");
        validRegister.setRole("PASSENGER");

        validLogin = new LoginRequest();
        validLogin.setEmail("passenger1@ridelink.lk");
        validLogin.setPassword("Passw0rd!");
    }

    @Test
    void registerSavesHashedPasswordAndReturnsToken() {
        when(users.existsByEmail("passenger1@ridelink.lk")).thenReturn(false);
        when(encoder.encode("Passw0rd!")).thenReturn("hashed");
        when(jwt.createToken(any(User.class))).thenReturn("jwt-token");
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("user-1");
            return u;
        });

        var res = service.register(validRegister);

        assertEquals("jwt-token", res.getToken());
        assertEquals("user-1", res.getUserId());
        assertEquals("PASSENGER", res.getRole());
        assertEquals("ACTIVE", res.getStatus());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        assertEquals("hashed", captor.getValue().getPasswordHash());
        assertEquals("passenger1@ridelink.lk", captor.getValue().getEmail());
    }

    @Test
    void registerThrowsWhenEmailExists() {
        when(users.existsByEmail("passenger1@ridelink.lk")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.register(validRegister));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("Email already registered", ex.getMessage());
        verify(users, never()).save(any());
    }

    @Test
    void loginThrowsWhenUserMissing() {
        when(users.findByEmail("passenger1@ridelink.lk")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.login(validLogin));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void loginThrowsWhenPasswordWrong() {
        User user = activeUser();
        when(users.findByEmail("passenger1@ridelink.lk")).thenReturn(Optional.of(user));
        when(encoder.matches("Passw0rd!", "hashed")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> service.login(validLogin));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void loginThrowsWhenAccountSuspended() {
        User user = activeUser();
        user.setStatus("SUSPENDED");
        when(users.findByEmail("passenger1@ridelink.lk")).thenReturn(Optional.of(user));
        when(encoder.matches("Passw0rd!", "hashed")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.login(validLogin));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertTrue(ex.getMessage().contains("SUSPENDED"));
    }

    @Test
    void loginReturnsTokenWhenActiveAndPasswordOk() {
        User user = activeUser();
        when(users.findByEmail("passenger1@ridelink.lk")).thenReturn(Optional.of(user));
        when(encoder.matches("Passw0rd!", "hashed")).thenReturn(true);
        when(jwt.createToken(user)).thenReturn("jwt-token");

        var res = service.login(validLogin);

        assertEquals("jwt-token", res.getToken());
        assertEquals("user-1", res.getUserId());
    }

    private User activeUser() {
        User user = new User();
        user.setId("user-1");
        user.setEmail("passenger1@ridelink.lk");
        user.setPasswordHash("hashed");
        user.setRole("PASSENGER");
        user.setStatus("ACTIVE");
        return user;
    }
}