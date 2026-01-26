package com.rcszh.gm.user.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(
        @NotBlank(message = "roleName is required")
        @Size(max = 64, message = "roleName too long")
        String roleName
) {
}

