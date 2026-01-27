package com.rcszh.gm.ow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.ow.entity.OwItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OwItemMapper extends BaseMapper<OwItem> {

    @Select("""
            SELECT i.*
            FROM ow_item i
            WHERE i.enabled = 1
              AND (#{category} IS NULL OR i.category = #{category})
              AND (#{quality} IS NULL OR i.quality = #{quality})
              AND (
                i.is_global = 1
                OR EXISTS (
                    SELECT 1
                    FROM ow_hero_item hi
                    JOIN ow_hero h ON h.id = hi.hero_id
                    WHERE hi.item_id = i.id
                      AND h.enabled = 1
                      AND h.hero_code = #{heroCode}
                )
              )
            ORDER BY i.sort_order ASC, i.id ASC
            """)
    List<OwItem> selectEnabledItemsForHero(@Param("heroCode") String heroCode,
                                          @Param("category") String category,
                                          @Param("quality") String quality);
}

