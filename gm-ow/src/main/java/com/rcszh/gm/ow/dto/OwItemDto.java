package com.rcszh.gm.ow.dto;

import java.util.Map;

public record OwItemDto(
        String itemCode,
        String itemName,
        Integer price,
        String quality,
        String category,
        String imgKey,
        String imgUrl,
        Map<String, Integer> stats,
        Boolean isGlobal
) {
}

