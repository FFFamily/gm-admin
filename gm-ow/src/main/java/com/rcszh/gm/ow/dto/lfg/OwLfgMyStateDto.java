package com.rcszh.gm.ow.dto.lfg;

public record OwLfgMyStateDto(
        boolean leader,
        boolean member,
        Long joinRequestId,
        String joinRequestStatus,
        boolean canJoin,
        boolean canRequest
) {
}
