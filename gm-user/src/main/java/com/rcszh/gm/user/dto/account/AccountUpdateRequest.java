package com.rcszh.gm.user.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountUpdateRequest(
        @NotBlank(message = "displayName is required")
        @Size(max = 64, message = "displayName too long")
        String displayName
) {
}

