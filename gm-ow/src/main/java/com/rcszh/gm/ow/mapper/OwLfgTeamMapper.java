package com.rcszh.gm.ow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.ow.entity.OwLfgTeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OwLfgTeamMapper extends BaseMapper<OwLfgTeam> {

    @Select("""
            SELECT *
            FROM ow_lfg_team
            WHERE id = #{id}
            FOR UPDATE
            """)
    OwLfgTeam selectForUpdateById(@Param("id") long id);

    @Select("""
            SELECT *
            FROM ow_lfg_team
            WHERE invite_code = #{inviteCode}
            LIMIT 1
            """)
    OwLfgTeam selectByInviteCode(@Param("inviteCode") String inviteCode);
}

