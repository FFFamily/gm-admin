package com.rcszh.gm.user.dto.auth;

import com.rcszh.gm.user.entity.SysUser;

import java.util.List;

public record CurrentUserDto(
        Long id,
        String username,
        String displayName,
        Boolean enabled,
        List<String> roleCodes,
        List<String> permCodes,
        Boolean isAdmin
) {
    public static CurrentUserDto from(SysUser u, List<String> roleCodes, List<String> permCodes) {
        boolean admin = roleCodes != null && roleCodes.stream().anyMatch("ADMIN"::equalsIgnoreCase);
        return new CurrentUserDto(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getEnabled(),
                roleCodes,
                permCodes,
                admin
        );
    }
}
