package com.rcszh.gm.config.security;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.user.entity.SysPermission;
import com.rcszh.gm.user.mapper.SysPermissionMapper;
import com.rcszh.gm.user.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleMapper userRoleMapper;
    private final SysPermissionMapper permissionMapper;

    public StpInterfaceImpl(SysUserRoleMapper userRoleMapper, SysPermissionMapper permissionMapper) {
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = LoginIdUtils.parseAdminId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }

        var roles = userRoleMapper.selectRoleCodesByUserId(userId);
        if (roles.stream().anyMatch("ADMIN"::equalsIgnoreCase)) {
            // Admin always has all permissions (including future ones).
            return permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().select(SysPermission::getPermCode))
                    .stream()
                    .map(SysPermission::getPermCode)
                    .toList();
        }
        return permissionMapper.selectPermCodesByUserId(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = LoginIdUtils.parseAdminId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        return userRoleMapper.selectRoleCodesByUserId(userId);
    }
}
