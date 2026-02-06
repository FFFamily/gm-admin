package com.rcszh.gm.ow.dto.lfg;

import java.time.LocalDateTime;
import java.util.List;

public record OwLfgTeamDetailDto(
        Long id,
        String inviteCode,
        String title,
        String modeCode,
        String platformCode,
        boolean allowCrossplay,
        int capacity,
        int memberCount,
        boolean autoApprove,
        int voiceRequired,
        String regionCode,
        String languageCode,
        String rankMin,
        String rankMax,
        List<String> needRoles,
        List<String> preferredHeroCodes,
        List<String> tags,
        String note,
        OwLfgContactDto contact, // only returned to members
        String leaderName,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        List<OwLfgMemberDto> members,
        List<OwLfgJoinRequestDto> pendingJoinRequests // only returned to leader
) {
}

