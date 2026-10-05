package com.ridelink.account.controller;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes account profile and status operations using the authenticated account identity.
 *
 * SOLID - Single Responsibility Principle: HTTP concerns remain in this controller; business
 * operations are delegated to the service layer.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/me")
    public AccountResponse me(Authentication auth) {
        return accounts.getMe(auth.getName());
    }

    @PutMapping("/me")
    public AccountResponse updateMe(Authentication auth, @RequestBody UpdateProfileRequest request) {
        return accounts.updateMe(auth.getName(), request);
    }

    @GetMapping("/{userId}")
    public AccountResponse getOne(Authentication auth, @PathVariable String userId) {
        return accounts.getByIdForCaller(auth.getName(), role(auth), userId);
    }

    @PatchMapping("/{userId}/status")
    public AccountResponse updateStatus(Authentication auth,
                                        @PathVariable String userId,
                                        @Valid @RequestBody UpdateStatusRequest request) {
        return accounts.updateStatus(role(auth), userId, request.getStatus());
    }

    private String role(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("");
    }
}