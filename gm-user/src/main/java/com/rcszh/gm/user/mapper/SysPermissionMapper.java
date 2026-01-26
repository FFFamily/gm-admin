package com.rcszh.gm.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.user.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("""
            SELECT DISTINCT p.perm_code
            FROM sys_user_role ur
            JOIN sys_role_permission rp ON rp.role_id = ur.role_id
            JOIN sys_permission p ON p.id = rp.perm_id
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT p.perm_code
            FROM sys_role_permission rp
            JOIN sys_permission p ON p.id = rp.perm_id
            WHERE rp.role_id = #{roleId}
            """)
    List<String> selectPermCodesByRoleId(@Param("roleId") Long roleId);
}
