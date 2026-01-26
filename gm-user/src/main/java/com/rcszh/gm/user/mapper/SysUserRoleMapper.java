package com.rcszh.gm.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.user.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("""
            SELECT DISTINCT r.role_code
            FROM sys_user_role ur
            JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}

