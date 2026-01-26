package com.rcszh.gm.user.dto.audit;

import com.rcszh.gm.user.entity.SysAuditLog;

import java.time.LocalDateTime;

public record AuditLogDto(
        Long id,
        Long actorUserId,
        String action,
        String targetType,
        String targetId,
        String detailJson,
        String result,
        String ip,
        String ua,
        String requestId,
        LocalDateTime createdAt
) {
    public static AuditLogDto from(SysAuditLog l) {
        return new AuditLogDto(
                l.getId(),
                l.getActorUserId(),
                l.getAction(),
                l.getTargetType(),
                l.getTargetId(),
                l.getDetailJson(),
                l.getResult(),
                l.getIp(),
                l.getUa(),
                l.getRequestId(),
                l.getCreatedAt()
        );
    }
}

