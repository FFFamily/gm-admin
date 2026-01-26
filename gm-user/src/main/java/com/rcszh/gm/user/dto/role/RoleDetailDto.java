package com.rcszh.gm.user.dto.role;

import com.rcszh.gm.user.entity.SysRole;

import java.time.LocalDateTime;
import java.util.List;

public record RoleDetailDto(
        Long id,
        String roleCode,
        String roleName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> permCodes
) {
    public static RoleDetailDto from(SysRole r, List<String> permCodes) {
        return new RoleDetailDto(r.getId(), r.getRoleCode(), r.getRoleName(), r.getCreatedAt(), r.getUpdatedAt(), permCodes);
    }
}

