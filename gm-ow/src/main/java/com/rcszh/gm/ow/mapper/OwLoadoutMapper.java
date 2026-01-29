package com.rcszh.gm.ow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.ow.dto.community.OwHeroLoadoutCountRow;
import com.rcszh.gm.ow.entity.OwLoadout;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OwLoadoutMapper extends BaseMapper<OwLoadout> {

    @Update("UPDATE ow_loadout SET view_count = view_count + 1 WHERE id = #{id} AND status = 'PUBLISHED'")
    int incView(@Param("id") long id);

    @Update("UPDATE ow_loadout SET like_count = like_count + 1 WHERE id = #{id}")
    int incLike(@Param("id") long id);

    @Update("UPDATE ow_loadout SET like_count = IF(like_count > 0, like_count - 1, 0) WHERE id = #{id}")
    int decLike(@Param("id") long id);

    @Update("UPDATE ow_loadout SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    int incFavorite(@Param("id") long id);

    @Update("UPDATE ow_loadout SET favorite_count = IF(favorite_count > 0, favorite_count - 1, 0) WHERE id = #{id}")
    int decFavorite(@Param("id") long id);

    @Select("SELECT hero_code AS heroCode, COUNT(1) AS cnt FROM ow_loadout WHERE status = 'PUBLISHED' GROUP BY hero_code")
    List<OwHeroLoadoutCountRow> countPublishedByHero();

    @Update("UPDATE ow_loadout SET status = #{status} WHERE id = #{id}")
    int setStatus(@Param("id") long id, @Param("status") String status);

    @Update("UPDATE ow_loadout SET is_featured = #{featured}, featured_at = IF(#{featured} = 1, CURRENT_TIMESTAMP, NULL) WHERE id = #{id}")
    int setFeatured(@Param("id") long id, @Param("featured") boolean featured);

    @Update("UPDATE ow_loadout SET is_pinned = #{pinned}, pinned_at = IF(#{pinned} = 1, CURRENT_TIMESTAMP, NULL) WHERE id = #{id}")
    int setPinned(@Param("id") long id, @Param("pinned") boolean pinned);
}
