package com.rcszh.gm.user.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserResetPasswordRequest(
        @NotBlank
        @Size(min = 6, max = 64)
        String newPassword
) {
}

