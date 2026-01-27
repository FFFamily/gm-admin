package com.rcszh.gm.user.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountRegisterRequest(
        @NotBlank(message = "username is required")
        @Size(max = 64, message = "username too long")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password too short")
        String password,

        @Size(max = 64, message = "displayName too long")
        String displayName
) {
}

