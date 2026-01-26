package com.rcszh.gm.api.auth;

import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.user.dto.auth.CurrentUserDto;
import com.rcszh.gm.user.dto.auth.LoginRequest;
import com.rcszh.gm.user.dto.auth.LoginResponse;
import com.rcszh.gm.user.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserDto> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}
