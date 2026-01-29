package com.rcszh.gm.ow.dto.admin;

import java.time.LocalDateTime;

public record OwAdminLoadoutDetailDto(
        Long id,
        String heroCode,
        String heroName,
        String title,
        String description,
        String itemsJson,
        Long createdByAccountId,
        String authorName,
        String status,
        Integer viewCount,
        Integer likeCount,
        Integer favoriteCount,
        Boolean featured,
        Boolean pinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

