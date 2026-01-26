package com.rcszh.gm.user.service.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.audit.AuditLogDto;
import com.rcszh.gm.user.entity.SysAuditLog;
import com.rcszh.gm.user.mapper.SysAuditLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditQueryService {

    private final SysAuditLogMapper auditLogMapper;

    public AuditQueryService(SysAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public PageResult<AuditLogDto> list(long page,
                                        long size,
                                        Long actorUserId,
                                        String action,
                                        String targetType,
                                        LocalDateTime startAt,
                                        LocalDateTime endAt) {
        var wrapper = new LambdaQueryWrapper<SysAuditLog>();
        if (actorUserId != null) {
            wrapper.eq(SysAuditLog::getActorUserId, actorUserId);
        }
        if (action != null && !action.isBlank()) {
            wrapper.eq(SysAuditLog::getAction, action.trim());
        }
        if (targetType != null && !targetType.isBlank()) {
            wrapper.eq(SysAuditLog::getTargetType, targetType.trim());
        }
        if (startAt != null) {
            wrapper.ge(SysAuditLog::getCreatedAt, startAt);
        }
        if (endAt != null) {
            wrapper.le(SysAuditLog::getCreatedAt, endAt);
        }
        wrapper.orderByDesc(SysAuditLog::getId);

        Page<SysAuditLog> p = auditLogMapper.selectPage(new Page<>(page, size), wrapper);
        List<AuditLogDto> records = p.getRecords().stream().map(AuditLogDto::from).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }
}

