package com.rcszh.gm.api.account;

import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.user.dto.account.AccountLoginRequest;
import com.rcszh.gm.user.dto.account.AccountLoginResponse;
import com.rcszh.gm.user.dto.account.AccountMeDto;
import com.rcszh.gm.user.dto.account.AccountRegisterRequest;
import com.rcszh.gm.user.dto.account.AccountUpdateRequest;
import com.rcszh.gm.user.service.account.AccountAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountAuthService accountAuthService;

    public AccountController(AccountAuthService accountAuthService) {
        this.accountAuthService = accountAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AccountLoginResponse> login(@Valid @RequestBody AccountLoginRequest req) {
        return ApiResponse.ok(accountAuthService.login(req));
    }

    @PostMapping("/register")
    public ApiResponse<AccountLoginResponse> register(@Valid @RequestBody AccountRegisterRequest req) {
        return ApiResponse.ok(accountAuthService.register(req));
    }

    @GetMapping("/me")
    public ApiResponse<AccountMeDto> me() {
        return ApiResponse.ok(accountAuthService.me());
    }

    @PutMapping("/me")
    public ApiResponse<AccountMeDto> updateMe(@Valid @RequestBody AccountUpdateRequest req) {
        return ApiResponse.ok(accountAuthService.updateMe(req));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        accountAuthService.logout();
        return ApiResponse.ok(null);
    }
}
