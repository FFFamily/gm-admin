package com.rcszh.gm.ow.dto.admin;

import jakarta.validation.constraints.NotNull;

public record OwLoadoutSetPinnedRequest(
        @NotNull Boolean pinned
) {
}

