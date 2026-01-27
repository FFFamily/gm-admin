package com.rcszh.gm.user.service.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rcszh.gm.common.exception.UnauthorizedException;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.common.web.RequestUtils;
import com.rcszh.gm.user.dto.auth.CurrentUserDto;
import com.rcszh.gm.user.dto.auth.LoginRequest;
import com.rcszh.gm.user.dto.auth.LoginResponse;
import com.rcszh.gm.user.entity.SysLoginLog;
import com.rcszh.gm.user.entity.SysPermission;
import com.rcszh.gm.user.entity.SysUser;
import com.rcszh.gm.user.mapper.SysLoginLogMapper;
import com.rcszh.gm.user.mapper.SysPermissionMapper;
import com.rcszh.gm.user.mapper.SysUserMapper;
import com.rcszh.gm.user.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysPermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper userMapper,
                       SysLoginLogMapper loginLogMapper,
                       SysUserRoleMapper userRoleMapper,
                       SysPermissionMapper permissionMapper,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest req) {
        String username = req.username().trim();

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));

        if (user == null) {
            writeLoginLog(username, false, "USER_NOT_FOUND");
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            writeLoginLog(username, false, "USER_DISABLED");
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            writeLoginLog(username, false, "BAD_PASSWORD");
            throw new IllegalArgumentException("Invalid username or password");
        }

        StpUtil.login(LoginIdUtils.adminLoginId(user.getId()));
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        writeLoginLog(username, true, null);

        return new LoginResponse(StpUtil.getTokenValue());
    }

    public CurrentUserDto currentUser() {
        Long userId = LoginIdUtils.parseAdminId(StpUtil.getLoginId());
        if (userId == null) {
            throw new UnauthorizedException("Not logged in");
        }
        SysUser u = userMapper.selectById(userId);
        if (u == null || Boolean.FALSE.equals(u.getEnabled())) {
            // Token exists but user is missing/disabled: treat as unauthorized.
            StpUtil.logout();
            throw new UnauthorizedException("Not logged in");
        }

        var roleCodes = userRoleMapper.selectRoleCodesByUserId(userId);
        var permCodes = roleCodes.stream().anyMatch("ADMIN"::equalsIgnoreCase)
                ? permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().select(SysPermission::getPermCode))
                .stream()
                .map(SysPermission::getPermCode)
                .toList()
                : permissionMapper.selectPermCodesByUserId(userId);

        return CurrentUserDto.from(u, roleCodes, permCodes);
    }

    private void writeLoginLog(String username, boolean success, String reason) {
        var log = new SysLoginLog();
        log.setUsername(username);
        log.setSuccess(success);
        log.setReason(reason);
        log.setIp(RequestUtils.clientIp());
        log.setUa(RequestUtils.userAgent());
        loginLogMapper.insert(log);
    }
}
