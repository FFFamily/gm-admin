package com.rcszh.gm.ow.dto.community;

import com.rcszh.gm.ow.dto.OwItemDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OwLoadoutDetailDto(
        Long id,
        String heroCode,
        String heroName,
        String title,
        String description,
        String authorName,
        LocalDateTime createdAt,
        Integer viewCount,
        Integer likeCount,
        Integer favoriteCount,
        Boolean featured,
        Boolean pinned,
        Boolean liked,
        Boolean favorited,
        List<OwItemDto> items,
        Integer totalPrice,
        Map<String, Integer> statTotals
) {
}
