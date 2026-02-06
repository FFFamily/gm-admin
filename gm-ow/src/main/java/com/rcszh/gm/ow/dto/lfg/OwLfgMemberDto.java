package com.rcszh.gm.ow.dto.lfg;

import java.time.LocalDateTime;
import java.util.List;

public record OwLfgMemberDto(
        Long id,
        Long accountId,
        String displayName,
        List<String> roleTags,
        boolean leader,
        LocalDateTime joinedAt
) {
}

