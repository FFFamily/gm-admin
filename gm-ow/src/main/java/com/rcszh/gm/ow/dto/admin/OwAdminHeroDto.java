package com.rcszh.gm.ow.dto.admin;

import com.rcszh.gm.ow.entity.OwHero;

public record OwAdminHeroDto(
        Long id,
        String heroCode,
        String heroName,
        Boolean enabled,
        Integer sortOrder,
        Integer initialGold
) {
    public static OwAdminHeroDto from(OwHero e) {
        return new OwAdminHeroDto(
                e.getId(),
                e.getHeroCode(),
                e.getHeroName(),
                e.getEnabled(),
                e.getSortOrder(),
                e.getInitialGold()
        );
    }
}

