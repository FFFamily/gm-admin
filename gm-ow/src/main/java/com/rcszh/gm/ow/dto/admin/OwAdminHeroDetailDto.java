package com.rcszh.gm.ow.dto.admin;

import java.util.Map;

public record OwAdminHeroDetailDto(
        Long id,
        String heroCode,
        String heroName,
        String description,
        String avatarKey,
        String avatarUrl,
        Integer initialGold,
        Map<String, Integer> baseStats,
        Boolean enabled,
        Integer sortOrder
) {
}

