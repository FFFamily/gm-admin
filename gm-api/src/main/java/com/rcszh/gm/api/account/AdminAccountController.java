package com.rcszh.gm.api.account;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.account.admin.AdminAccountDetailDto;
import com.rcszh.gm.user.dto.account.admin.AdminAccountDto;
import com.rcszh.gm.user.dto.account.admin.AdminAccountResetPasswordRequest;
import com.rcszh.gm.user.dto.account.admin.AdminAccountUpdateRequest;
import com.rcszh.gm.user.service.account.AccountAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AdminAccountController {

    private final AccountAdminService accountAdminService;

    public AdminAccountController(AccountAdminService accountAdminService) {
        this.accountAdminService = accountAdminService;
    }

    @SaCheckPermission("account:list")
    @GetMapping
    public ApiResponse<PageResult<AdminAccountDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.ok(accountAdminService.list(page, size, keyword, enabled));
    }

    @SaCheckPermission("account:read")
    @GetMapping("/{id}")
    public ApiResponse<AdminAccountDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(accountAdminService.detail(id));
    }

    @SaCheckPermission("account:update")
    @PutMapping("/{id}")
    public ApiResponse<AdminAccountDetailDto> update(@PathVariable("id") long id, @Valid @RequestBody AdminAccountUpdateRequest req) {
        return ApiResponse.ok(accountAdminService.update(id, req));
    }

    @SaCheckPermission("account:reset_password")
    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable("id") long id, @Valid @RequestBody AdminAccountResetPasswordRequest req) {
        accountAdminService.resetPassword(id, req);
        return ApiResponse.ok(null);
    }
}
