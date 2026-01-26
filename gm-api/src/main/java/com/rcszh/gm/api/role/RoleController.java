package com.rcszh.gm.api.role;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.role.*;
import com.rcszh.gm.user.service.role.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @SaCheckPermission("role:list")
    @GetMapping
    public ApiResponse<PageResult<RoleDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) long size
    ) {
        return ApiResponse.ok(roleService.list(page, size));
    }

    @SaCheckPermission("role:read")
    @GetMapping("/{id}")
    public ApiResponse<RoleDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(roleService.detail(id));
    }

    @SaCheckPermission("role:create")
    @PostMapping
    public ApiResponse<RoleDetailDto> create(@Valid @RequestBody RoleCreateRequest req) {
        return ApiResponse.ok(roleService.create(req));
    }

    @SaCheckPermission("role:update")
    @PutMapping("/{id}")
    public ApiResponse<RoleDetailDto> update(@PathVariable("id") long id, @Valid @RequestBody RoleUpdateRequest req) {
        return ApiResponse.ok(roleService.update(id, req));
    }

    @SaCheckPermission("role:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") long id) {
        roleService.delete(id);
        return ApiResponse.ok(null);
    }

    @SaCheckPermission("role:bind_permissions")
    @PostMapping("/{id}/permissions")
    public ApiResponse<RoleDetailDto> bindPermissions(@PathVariable("id") long id, @Valid @RequestBody RoleBindPermissionsRequest req) {
        return ApiResponse.ok(roleService.bindPermissions(id, req));
    }
}
