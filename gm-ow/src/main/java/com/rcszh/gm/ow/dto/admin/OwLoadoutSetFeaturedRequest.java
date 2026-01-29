package com.rcszh.gm.ow.dto.admin;

import jakarta.validation.constraints.NotNull;

public record OwLoadoutSetFeaturedRequest(
        @NotNull Boolean featured
) {
}

