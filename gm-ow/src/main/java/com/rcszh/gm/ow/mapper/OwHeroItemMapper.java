package com.rcszh.gm.ow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rcszh.gm.ow.entity.OwHeroItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OwHeroItemMapper extends BaseMapper<OwHeroItem> {

    @Select("""
            SELECT hero_id
            FROM ow_hero_item
            WHERE item_id = #{itemId}
            """)
    List<Long> selectHeroIdsByItemId(@Param("itemId") Long itemId);
}

