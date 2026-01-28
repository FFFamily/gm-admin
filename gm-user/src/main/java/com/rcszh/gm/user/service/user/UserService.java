package com.rcszh.gm.user.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.user.UserCreateRequest;
import com.rcszh.gm.user.dto.user.UserDetailDto;
import com.rcszh.gm.user.dto.user.UserDto;
import com.rcszh.gm.user.dto.user.UserResetPasswordRequest;
import com.rcszh.gm.user.dto.user.UserUpdateRequest;
import com.rcszh.gm.user.entity.SysRole;
import com.rcszh.gm.user.entity.SysUser;
import com.rcszh.gm.user.entity.SysUserRole;
import com.rcszh.gm.user.mapper.SysRoleMapper;
import com.rcszh.gm.user.mapper.SysUserMapper;
import com.rcszh.gm.user.mapper.SysUserRoleMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(SysUserMapper userMapper,
                       SysRoleMapper roleMapper,
                       SysUserRoleMapper userRoleMapper,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public PageResult<UserDto> list(long page, long size, String keyword, Boolean enabled) {
        var wrapper = new LambdaQueryWrapper<SysUser>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, kw).or().like(SysUser::getDisplayName, kw));
        }
        if (enabled != null) {
            wrapper.eq(SysUser::getEnabled, enabled);
        }
        wrapper.orderByDesc(SysUser::getId);

        Page<SysUser> p = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserDto> records = p.getRecords().stream().map(UserDto::from).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }

    public UserDetailDto detail(long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }
        var roleCodes = userRoleMapper.selectRoleCodesByUserId(u.getId());
        return UserDetailDto.from(u, roleCodes);
    }

    @Transactional
    public UserDetailDto create(UserCreateRequest req) {
        String username = req.username().trim();
        if (userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username).last("LIMIT 1")) != null) {
            throw new IllegalStateException("Username already exists");
        }

        var u = new SysUser();
        u.setUsername(username);
        u.setDisplayName(req.displayName().trim());
        u.setEnabled(req.enabled());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setLastLoginAt(null);
        userMapper.insert(u);

        replaceUserRoles(u.getId(), req.roleCodes());

        auditLogService.success("USER_CREATE", "USER", String.valueOf(u.getId()), req);
        return detail(u.getId());
    }

    @Transactional
    public UserDetailDto update(long id, UserUpdateRequest req) {
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }

        u.setDisplayName(req.displayName().trim());
        u.setEnabled(req.enabled());
        userMapper.updateById(u);

        replaceUserRoles(u.getId(), req.roleCodes());

        auditLogService.success("USER_UPDATE", "USER", String.valueOf(u.getId()), req);
        return detail(u.getId());
    }

    @Transactional
    public void resetPassword(long id, UserResetPasswordRequest req) {
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }

        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userMapper.updateById(u);

        auditLogService.success("USER_RESET_PASSWORD", "USER", String.valueOf(u.getId()), null);
    }

    private void replaceUserRoles(Long userId, List<String> roleCodes) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }

        var roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, roleCodes));
        if (roles.size() != roleCodes.size()) {
            throw new IllegalArgumentException("Role not found");
        }

        for (var r : roles) {
            var ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(r.getId());
            userRoleMapper.insert(ur);
        }
    }

}
