package com.rcszh.gm.ow.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record OwItemUpdateRequest(
        @NotBlank String itemName,
        @NotNull Integer price,
        @NotBlank String quality,
        @NotBlank String category,
        String imgKey,
        String imgUrl,
        @NotNull Map<String, Integer> stats,
        @NotNull Boolean isGlobal,
        List<Long> heroIds,
        @NotNull Boolean enabled,
        Integer sortOrder
) {
}

