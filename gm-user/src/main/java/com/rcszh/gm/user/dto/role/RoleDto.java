package com.rcszh.gm.user.dto.role;

import com.rcszh.gm.user.entity.SysRole;

import java.time.LocalDateTime;

public record RoleDto(
        Long id,
        String roleCode,
        String roleName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RoleDto from(SysRole r) {
        return new RoleDto(r.getId(), r.getRoleCode(), r.getRoleName(), r.getCreatedAt(), r.getUpdatedAt());
    }
}

