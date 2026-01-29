package com.rcszh.gm.ow.dto.community;

public record OwLoadoutItemPreviewDto(
        String itemCode,
        String itemName,
        Integer price,
        String quality,
        String category,
        String imgKey,
        String imgUrl
) {
}

