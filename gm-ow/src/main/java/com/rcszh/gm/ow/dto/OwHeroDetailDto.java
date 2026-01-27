package com.rcszh.gm.ow.dto;

import java.util.Map;

public record OwHeroDetailDto(
        String heroCode,
        String heroName,
        String description,
        String avatarKey,
        String avatarUrl,
        Integer initialGold,
        Map<String, Integer> baseStats
) {
}

