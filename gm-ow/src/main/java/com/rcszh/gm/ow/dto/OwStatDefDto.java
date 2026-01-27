package com.rcszh.gm.ow.dto;

import com.rcszh.gm.ow.entity.OwStatDef;

public record OwStatDefDto(
        String key,
        String label,
        String unit,
        Boolean isPercent,
        String iconName,
        String colorClass,
        Integer defaultValue,
        Integer sortOrder
) {
    public static OwStatDefDto from(OwStatDef e) {
        return new OwStatDefDto(
                e.getStatKey(),
                e.getLabel(),
                e.getUnit(),
                e.getIsPercent(),
                e.getIconName(),
                e.getColorClass(),
                e.getDefaultValue(),
                e.getSortOrder()
        );
    }
}

