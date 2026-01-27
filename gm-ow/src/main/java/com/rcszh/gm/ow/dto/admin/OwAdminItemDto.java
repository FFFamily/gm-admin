package com.rcszh.gm.ow.dto.admin;

import com.rcszh.gm.ow.entity.OwItem;

public record OwAdminItemDto(
        Long id,
        String itemCode,
        String itemName,
        Integer price,
        String quality,
        String category,
        Boolean isGlobal,
        Boolean enabled,
        Integer sortOrder
) {
    public static OwAdminItemDto from(OwItem e) {
        return new OwAdminItemDto(
                e.getId(),
                e.getItemCode(),
                e.getItemName(),
                e.getPrice(),
                e.getQuality(),
                e.getCategory(),
                e.getIsGlobal(),
                e.getEnabled(),
                e.getSortOrder()
        );
    }
}

