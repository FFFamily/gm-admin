package com.rcszh.gm.user.service.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.account.admin.AdminAccountDetailDto;
import com.rcszh.gm.user.dto.account.admin.AdminAccountDto;
import com.rcszh.gm.user.dto.account.admin.AdminAccountResetPasswordRequest;
import com.rcszh.gm.user.dto.account.admin.AdminAccountUpdateRequest;
import com.rcszh.gm.user.entity.AppAccount;
import com.rcszh.gm.user.mapper.AppAccountMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountAdminService {

    private final AppAccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AccountAdminService(AppAccountMapper accountMapper, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public PageResult<AdminAccountDto> list(long page, long size, String keyword, Boolean enabled) {
        var wrapper = new LambdaQueryWrapper<AppAccount>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(AppAccount::getUsername, kw).or().like(AppAccount::getDisplayName, kw));
        }
        if (enabled != null) {
            wrapper.eq(AppAccount::getEnabled, enabled);
        }
        wrapper.orderByDesc(AppAccount::getId);

        Page<AppAccount> p = accountMapper.selectPage(new Page<>(page, size), wrapper);
        List<AdminAccountDto> records = p.getRecords().stream().map(AdminAccountDto::from).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }

    public AdminAccountDetailDto detail(long id) {
        AppAccount a = accountMapper.selectById(id);
        if (a == null) {
            throw new IllegalArgumentException("Account not found");
        }
        return AdminAccountDetailDto.from(a);
    }

    @Transactional
    public AdminAccountDetailDto update(long id, AdminAccountUpdateRequest req) {
        AppAccount a = accountMapper.selectById(id);
        if (a == null) {
            throw new IllegalArgumentException("Account not found");
        }
        a.setDisplayName(req.displayName().trim());
        a.setEnabled(req.enabled());
        accountMapper.updateById(a);

        auditLogService.success("ACCOUNT_UPDATE", "ACCOUNT", String.valueOf(id), req);
        return AdminAccountDetailDto.from(a);
    }

    @Transactional
    public void resetPassword(long id, AdminAccountResetPasswordRequest req) {
        AppAccount a = accountMapper.selectById(id);
        if (a == null) {
            throw new IllegalArgumentException("Account not found");
        }
        a.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        accountMapper.updateById(a);

        auditLogService.success("ACCOUNT_RESET_PASSWORD", "ACCOUNT", String.valueOf(id), null);
    }
}
