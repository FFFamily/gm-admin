package com.rcszh.gm.user.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserUpdateRequest(
        @NotBlank(message = "displayName is required")
        @Size(max = 64, message = "displayName too long")
        String displayName,

        @NotNull(message = "enabled is required")
        Boolean enabled,

        List<String> roleCodes
) {
}

