package com.rcszh.gm.ow.dto.community;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OwLoadoutSummaryDto(
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
        Integer totalPrice,
        Map<String, Integer> statTotals,
        List<OwLoadoutTopStatDto> topStats,
        OwLoadoutCategoryCountsDto categoryCounts,
        List<OwLoadoutItemPreviewDto> itemsPreview
) {
}
