package com.rcszh.gm.user.dto.account.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminAccountUpdateRequest(
        @NotBlank(message = "displayName is required")
        @Size(max = 64, message = "displayName too long")
        String displayName,

        @NotNull(message = "enabled is required")
        Boolean enabled
) {
}

