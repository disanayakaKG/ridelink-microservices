package com.ridelink.account.controller;

import com.ridelink.account.dto.InternalAccountResponse;
import com.ridelink.account.exception.ApiException;
import com.ridelink.account.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Provides account role/status lookup for service consumers. The internal route is permitted by
 * the security chain, so this controller checks the X-Internal-Key header.
 */
@RestController
@RequestMapping("/api/v1/internal/accounts")
public class InternalAccountController {

    private final AccountService accounts;
    private final String internalKey;

    public InternalAccountController(
            AccountService accounts,
            @Value("${internal.api-key}") String internalKey
    ) {
        this.accounts = accounts;
        this.internalKey = internalKey;
    }

    @GetMapping("/{userId}")
    public InternalAccountResponse getOne(
            @RequestHeader(value = "X-Internal-Key", required = false) String key,
            @PathVariable String userId
    ) {
        if (key == null || !key.equals(internalKey)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid internal key");
        }
        return accounts.getInternal(userId);
    }
}