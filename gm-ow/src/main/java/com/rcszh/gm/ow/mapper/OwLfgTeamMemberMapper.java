package com.rcszh.gm.ow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.ow.entity.OwLfgTeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OwLfgTeamMemberMapper extends BaseMapper<OwLfgTeamMember> {

    @Select("""
            SELECT *
            FROM ow_lfg_team_member
            WHERE team_id = #{teamId}
              AND status = 'JOINED'
            ORDER BY joined_at ASC, id ASC
            """)
    List<OwLfgTeamMember> listJoined(@Param("teamId") long teamId);
}

