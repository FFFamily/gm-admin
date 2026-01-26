package com.rcszh.gm.api.permission;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.user.dto.permission.PermissionDto;
import com.rcszh.gm.user.service.permission.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @SaCheckPermission("permission:list")
    @GetMapping
    public ApiResponse<List<PermissionDto>> list() {
        return ApiResponse.ok(permissionService.list());
    }
}
