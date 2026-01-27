package com.rcszh.gm.ow.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record OwHeroCreateRequest(
        @NotBlank String heroCode,
        @NotBlank String heroName,
        String description,
        String avatarKey,
        String avatarUrl,
        Integer initialGold,
        @NotNull Map<String, Integer> baseStats,
        @NotNull Boolean enabled,
        Integer sortOrder
) {
}

