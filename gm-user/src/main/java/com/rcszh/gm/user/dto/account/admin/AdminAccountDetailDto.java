package com.rcszh.gm.user.dto.account.admin;

import com.rcszh.gm.user.entity.AppAccount;

import java.time.LocalDateTime;

public record AdminAccountDetailDto(
        Long id,
        String username,
        String displayName,
        Boolean enabled,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminAccountDetailDto from(AppAccount a) {
        return new AdminAccountDetailDto(
                a.getId(),
                a.getUsername(),
                a.getDisplayName(),
                a.getEnabled(),
                a.getLastLoginAt(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}

