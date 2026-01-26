package com.rcszh.gm.user.dto.user;

import com.rcszh.gm.user.entity.SysUser;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String username,
        String displayName,
        Boolean enabled,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserDto from(SysUser u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getEnabled(),
                u.getLastLoginAt(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}

