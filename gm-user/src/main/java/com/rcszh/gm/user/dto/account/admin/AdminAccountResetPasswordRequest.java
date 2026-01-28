package com.rcszh.gm.user.dto.account.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminAccountResetPasswordRequest(
        @NotBlank
        @Size(min = 6, max = 64)
        String newPassword
) {
}

