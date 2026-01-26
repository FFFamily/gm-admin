package com.rcszh.gm.user.service.audit;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.common.web.RequestUtils;
import com.rcszh.gm.user.entity.SysAuditLog;
import com.rcszh.gm.user.mapper.SysAuditLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final SysAuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public AuditLogService(SysAuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    public void success(String action, String targetType, String targetId, Object detail) {
        write(action, targetType, targetId, detail, "SUCCESS");
    }

    public void fail(String action, String targetType, String targetId, Object detail) {
        write(action, targetType, targetId, detail, "FAIL");
    }

    private void write(String action, String targetType, String targetId, Object detail, String result) {
        var log = new SysAuditLog();
        log.setActorUserId(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetailJson(toJson(detail));
        log.setResult(result);
        log.setIp(RequestUtils.clientIp());
        log.setUa(RequestUtils.userAgent());
        log.setRequestId(RequestUtils.requestId());
        auditLogMapper.insert(log);
    }

    private String toJson(Object detail) {
        if (detail == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            return "{\"_error\":\"json_serialize_failed\"}";
        }
    }
}

