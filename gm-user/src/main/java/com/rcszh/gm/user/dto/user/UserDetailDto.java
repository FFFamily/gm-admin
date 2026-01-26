package com.rcszh.gm.user.dto.user;

import com.rcszh.gm.user.entity.SysUser;

import java.time.LocalDateTime;
import java.util.List;

public record UserDetailDto(
        Long id,
        String username,
        String displayName,
        Boolean enabled,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> roleCodes
) {
    public static UserDetailDto from(SysUser u, List<String> roleCodes) {
        return new UserDetailDto(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getEnabled(),
                u.getLastLoginAt(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                roleCodes
        );
    }
}

