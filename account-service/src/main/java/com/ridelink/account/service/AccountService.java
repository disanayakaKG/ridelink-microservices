package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.AuthResponse;
import com.ridelink.account.dto.InternalAccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.exception.ApiException;
import com.ridelink.account.model.User;
import com.ridelink.account.repository.UserRepository;
import com.ridelink.account.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Coordinates registration, authentication and account profile/status rules.
 *
 * SOLID - Dependency Inversion Principle: persistence and password operations use injected
 * UserRepository and PasswordEncoder abstractions.
 */
@Service
public class AccountService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AccountService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /**
     * Normalizes email, rejects duplicates, hashes the password and creates an ACTIVE account
     * before issuing its JWT.
     */
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setFullName(req.getFullName().trim());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        user.setStatus("ACTIVE");
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        users.save(user);

        return toAuth(user);
    }

    /**
     * Checks the password hash and rejects non-ACTIVE accounts before issuing a JWT.
     */
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is " + user.getStatus());
        }
        return toAuth(user);
    }

    public AccountResponse getById(String userId) {
        return toAccount(find(userId));
    }

    public AccountResponse getMe(String userId) {
        return getById(userId);
    }

    /**
     * Applies supplied profile changes without changing role, status or password.
     */
    public AccountResponse updateMe(String userId, UpdateProfileRequest req) {
        User user = find(userId);
        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        user.setUpdatedAt(Instant.now());
        users.save(user);
        return toAccount(user);
    }

    /**
     * Allows account lookup by its owner or an ADMIN caller.
     */
    public AccountResponse getByIdForCaller(String callerId, String callerRole, String targetId) {
        if (!targetId.equals(callerId) && !"ADMIN".equals(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to view this account");
        }
        return getById(targetId);
    }

    /**
     * Requires an ADMIN caller before persisting the requested account status.
     */
    public AccountResponse updateStatus(String callerRole, String targetId, String status) {
        if (!"ADMIN".equals(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin only");
        }
        User user = find(targetId);
        user.setStatus(status);
        user.setUpdatedAt(Instant.now());
        users.save(user);
        return toAccount(user);
    }

    /**
     * Returns role/status metadata and derives active from the ACTIVE status.
     */
    public InternalAccountResponse getInternal(String userId) {
        User user = find(userId);
        return new InternalAccountResponse(
                user.getId(),
                user.getRole(),
                user.getStatus(),
                "ACTIVE".equals(user.getStatus())
        );
    }

    private User find(String userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AuthResponse toAuth(User user) {
        return new AuthResponse(
                jwt.createToken(user),
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    private AccountResponse toAccount(User user) {
        AccountResponse res = new AccountResponse();
        res.setUserId(user.getId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setPhone(user.getPhone());
        res.setRole(user.getRole());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }
}