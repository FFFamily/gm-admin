package com.rcszh.gm.ow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.ow.entity.OwLfgTeamJoinRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OwLfgTeamJoinRequestMapper extends BaseMapper<OwLfgTeamJoinRequest> {

    @Select("""
            SELECT *
            FROM ow_lfg_team_join_request
            WHERE team_id = #{teamId}
              AND status = #{status}
            ORDER BY created_at ASC, id ASC
            """)
    List<OwLfgTeamJoinRequest> listByTeamAndStatus(@Param("teamId") long teamId, @Param("status") String status);

    @Select("""
            SELECT *
            FROM ow_lfg_team_join_request
            WHERE id = #{id}
            FOR UPDATE
            """)
    OwLfgTeamJoinRequest selectForUpdateById(@Param("id") long id);
}

