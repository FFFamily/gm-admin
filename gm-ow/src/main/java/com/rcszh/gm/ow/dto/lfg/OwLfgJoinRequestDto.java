package com.rcszh.gm.ow.dto.lfg;

import java.time.LocalDateTime;

public record OwLfgJoinRequestDto(
        Long id,
        Long teamId,
        Long accountId,
        String displayName,
        String message,
        String status,
        LocalDateTime createdAt
) {
}

