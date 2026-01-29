package com.rcszh.gm.ow.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record OwLoadoutSetStatusRequest(
        @NotBlank String status
) {
}

