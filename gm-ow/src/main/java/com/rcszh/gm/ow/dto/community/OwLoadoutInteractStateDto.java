package com.rcszh.gm.ow.dto.community;

public record OwLoadoutInteractStateDto(
        Long loadoutId,
        Integer likeCount,
        Integer favoriteCount,
        Boolean liked,
        Boolean favorited
) {
}

