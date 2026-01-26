package com.rcszh.gm.user.service.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.user.dto.role.RoleBindPermissionsRequest;
import com.rcszh.gm.user.dto.role.RoleCreateRequest;
import com.rcszh.gm.user.dto.role.RoleDetailDto;
import com.rcszh.gm.user.dto.role.RoleDto;
import com.rcszh.gm.user.dto.role.RoleUpdateRequest;
import com.rcszh.gm.user.entity.SysPermission;
import com.rcszh.gm.user.entity.SysRole;
import com.rcszh.gm.user.entity.SysRolePermission;
import com.rcszh.gm.user.mapper.SysPermissionMapper;
import com.rcszh.gm.user.mapper.SysRoleMapper;
import com.rcszh.gm.user.mapper.SysRolePermissionMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final AuditLogService auditLogService;

    public RoleService(SysRoleMapper roleMapper,
                       SysPermissionMapper permissionMapper,
                       SysRolePermissionMapper rolePermissionMapper,
                       AuditLogService auditLogService) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.auditLogService = auditLogService;
    }

    public PageResult<RoleDto> list(long page, long size) {
        Page<SysRole> p = roleMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
        var records = p.getRecords().stream().map(RoleDto::from).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }

    public RoleDetailDto detail(long id) {
        SysRole r = roleMapper.selectById(id);
        if (r == null) {
            throw new IllegalArgumentException("Role not found");
        }
        List<String> permCodes = permissionMapper.selectPermCodesByRoleId(r.getId());
        return RoleDetailDto.from(r, permCodes);
    }

    @Transactional
    public RoleDetailDto create(RoleCreateRequest req) {
        String roleCode = req.roleCode().trim();
        if (roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode).last("LIMIT 1")) != null) {
            throw new IllegalStateException("Role code already exists");
        }
        var r = new SysRole();
        r.setRoleCode(roleCode);
        r.setRoleName(req.roleName().trim());
        roleMapper.insert(r);

        auditLogService.success("ROLE_CREATE", "ROLE", String.valueOf(r.getId()), req);
        return detail(r.getId());
    }

    @Transactional
    public RoleDetailDto update(long id, RoleUpdateRequest req) {
        SysRole r = roleMapper.selectById(id);
        if (r == null) {
            throw new IllegalArgumentException("Role not found");
        }
        r.setRoleName(req.roleName().trim());
        roleMapper.updateById(r);

        auditLogService.success("ROLE_UPDATE", "ROLE", String.valueOf(r.getId()), req);
        return detail(r.getId());
    }

    @Transactional
    public void delete(long id) {
        SysRole r = roleMapper.selectById(id);
        if (r == null) {
            return;
        }
        if (ROLE_ADMIN.equalsIgnoreCase(r.getRoleCode())) {
            throw new IllegalStateException("ADMIN role cannot be deleted");
        }
        roleMapper.deleteById(id);
        auditLogService.success("ROLE_DELETE", "ROLE", String.valueOf(id), null);
    }

    @Transactional
    public RoleDetailDto bindPermissions(long id, RoleBindPermissionsRequest req) {
        SysRole r = roleMapper.selectById(id);
        if (r == null) {
            throw new IllegalArgumentException("Role not found");
        }

        var permCodes = req.permCodes() == null ? List.<String>of() : req.permCodes().stream().map(String::trim).toList();
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));

        if (!permCodes.isEmpty()) {
            var perms = permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().in(SysPermission::getPermCode, permCodes));
            if (perms.size() != permCodes.size()) {
                throw new IllegalArgumentException("Permission not found");
            }
            for (var p : perms) {
                var rp = new SysRolePermission();
                rp.setRoleId(id);
                rp.setPermId(p.getId());
                rolePermissionMapper.insert(rp);
            }
        }

        auditLogService.success("ROLE_BIND_PERMISSIONS", "ROLE", String.valueOf(id), req);
        return detail(id);
    }
}

