package com.rcszh.gm.ow.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface OwLoadoutLikeMapper {

    @Insert("INSERT IGNORE INTO ow_loadout_like(loadout_id, account_id) VALUES (#{loadoutId}, #{accountId})")
    int insertIgnore(@Param("loadoutId") long loadoutId, @Param("accountId") long accountId);

    @Delete("DELETE FROM ow_loadout_like WHERE loadout_id = #{loadoutId} AND account_id = #{accountId}")
    int deleteOne(@Param("loadoutId") long loadoutId, @Param("accountId") long accountId);

    @Select("SELECT COUNT(1) FROM ow_loadout_like WHERE loadout_id = #{loadoutId} AND account_id = #{accountId}")
    int exists(@Param("loadoutId") long loadoutId, @Param("accountId") long accountId);
}

