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

    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(req.getEmail().trim().toLowerCase())
            // Keep missing-user and wrong-password responses identical to avoid account enumeration.
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

    public AccountResponse getByIdForCaller(String callerId, String callerRole, String targetId) {
        // Users may read their own account; only administrators may read another user's account.
        if (!targetId.equals(callerId) && !"ADMIN".equals(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to view this account");
        }
        return getById(targetId);
    }

    public AccountResponse updateStatus(String callerRole, String targetId, String status) {
        // Account status changes are administrative operations, not self-service profile updates.
        if (!"ADMIN".equals(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin only");
        }
        User user = find(targetId);
        user.setStatus(status);
        user.setUpdatedAt(Instant.now());
        users.save(user);
        return toAccount(user);
    }

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