package com.rcszh.gm.api.audit;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.audit.AuditLogDto;
import com.rcszh.gm.user.service.audit.AuditQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @SaCheckPermission("audit:list")
    @GetMapping
    public ApiResponse<PageResult<AuditLogDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
    ) {
        return ApiResponse.ok(auditQueryService.list(page, size, actorUserId, action, targetType, startAt, endAt));
    }
}
