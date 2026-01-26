package com.rcszh.gm.user.service.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rcszh.gm.user.dto.permission.PermissionDto;
import com.rcszh.gm.user.entity.SysPermission;
import com.rcszh.gm.user.mapper.SysPermissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final SysPermissionMapper permissionMapper;

    public PermissionService(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public List<PermissionDto> list() {
        return permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getId))
                .stream()
                .map(PermissionDto::from)
                .toList();
    }
}

