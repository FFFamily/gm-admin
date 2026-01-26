package com.rcszh.gm.user.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserCreateRequest(
        @NotBlank(message = "username is required")
        @Size(max = 64, message = "username too long")
        String username,

        @NotBlank(message = "displayName is required")
        @Size(max = 64, message = "displayName too long")
        String displayName,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password too short")
        String password,

        @NotNull(message = "enabled is required")
        Boolean enabled,

        List<String> roleCodes
) {
}

