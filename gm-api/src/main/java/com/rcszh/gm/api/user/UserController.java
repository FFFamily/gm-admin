package com.rcszh.gm.api.user;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.user.*;
import com.rcszh.gm.user.service.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @SaCheckPermission("user:list")
    @GetMapping
    public ApiResponse<PageResult<UserDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.ok(userService.list(page, size, keyword, enabled));
    }

    @SaCheckPermission("user:read")
    @GetMapping("/{id}")
    public ApiResponse<UserDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(userService.detail(id));
    }

    @SaCheckPermission("user:create")
    @PostMapping
    public ApiResponse<UserDetailDto> create(@Valid @RequestBody UserCreateRequest req) {
        return ApiResponse.ok(userService.create(req));
    }

    @SaCheckPermission("user:update")
    @PutMapping("/{id}")
    public ApiResponse<UserDetailDto> update(@PathVariable("id") long id, @Valid @RequestBody UserUpdateRequest req) {
        return ApiResponse.ok(userService.update(id, req));
    }

    @SaCheckPermission("user:reset_password")
    @PostMapping("/{id}/reset-password")
    public ApiResponse<UserResetPasswordResponse> resetPassword(@PathVariable("id") long id) {
        return ApiResponse.ok(userService.resetPassword(id));
    }
}
