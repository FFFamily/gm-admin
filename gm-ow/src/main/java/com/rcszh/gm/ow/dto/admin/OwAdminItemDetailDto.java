package com.rcszh.gm.ow.dto.admin;

import java.util.List;
import java.util.Map;

public record OwAdminItemDetailDto(
        Long id,
        String itemCode,
        String itemName,
        Integer price,
        String quality,
        String category,
        String imgKey,
        String imgUrl,
        Map<String, Integer> stats,
        Boolean isGlobal,
        List<Long> heroIds,
        Boolean enabled,
        Integer sortOrder
) {
}

