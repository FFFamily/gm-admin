package com.rcszh.gm.ow.dto;

import com.rcszh.gm.ow.entity.OwHero;

public record OwHeroSummaryDto(
        String heroCode,
        String heroName,
        String description,
        String avatarKey,
        String avatarUrl
) {
    public static OwHeroSummaryDto from(OwHero e) {
        return new OwHeroSummaryDto(
                e.getHeroCode(),
                e.getHeroName(),
                e.getDescription(),
                e.getAvatarKey(),
                e.getAvatarUrl()
        );
    }
}

