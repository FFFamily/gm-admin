package com.rcszh.gm.user.dto.permission;

import com.rcszh.gm.user.entity.SysPermission;

import java.time.LocalDateTime;

public record PermissionDto(
        Long id,
        String permCode,
        String permName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PermissionDto from(SysPermission p) {
        return new PermissionDto(p.getId(), p.getPermCode(), p.getPermName(), p.getCreatedAt(), p.getUpdatedAt());
    }
}

