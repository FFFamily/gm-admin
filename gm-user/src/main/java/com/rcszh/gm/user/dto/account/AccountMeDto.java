package com.rcszh.gm.user.dto.account;

import com.rcszh.gm.user.entity.AppAccount;

public record AccountMeDto(
        Long id,
        String username,
        String displayName
) {
    public static AccountMeDto from(AppAccount a) {
        return new AccountMeDto(a.getId(), a.getUsername(), a.getDisplayName());
    }
}
