package com.rcszh.gm.user.dto.account;

import jakarta.validation.constraints.NotBlank;

public record AccountLoginRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required") String password
) {
}

