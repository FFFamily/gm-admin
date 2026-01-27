package com.rcszh.gm.ow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.ow.dto.*;
import com.rcszh.gm.ow.entity.OwConfig;
import com.rcszh.gm.ow.entity.OwHero;
import com.rcszh.gm.ow.entity.OwItem;
import com.rcszh.gm.ow.entity.OwStatDef;
import com.rcszh.gm.ow.mapper.OwConfigMapper;
import com.rcszh.gm.ow.mapper.OwHeroMapper;
import com.rcszh.gm.ow.mapper.OwItemMapper;
import com.rcszh.gm.ow.mapper.OwStatDefMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OwPublicQueryService {

    private static final String CONFIG_INITIAL_GOLD = "initial_gold";

    private final OwStatDefMapper statDefMapper;
    private final OwConfigMapper configMapper;
    private final OwHeroMapper heroMapper;
    private final OwItemMapper itemMapper;
    private final ObjectMapper objectMapper;

    public OwPublicQueryService(OwStatDefMapper statDefMapper,
                                OwConfigMapper configMapper,
                                OwHeroMapper heroMapper,
                                OwItemMapper itemMapper,
                                ObjectMapper objectMapper) {
        this.statDefMapper = statDefMapper;
        this.configMapper = configMapper;
        this.heroMapper = heroMapper;
        this.itemMapper = itemMapper;
        this.objectMapper = objectMapper;
    }

    public OwBaseInfoDto baseInfo() {
        Integer initialGold = readConfigInt(CONFIG_INITIAL_GOLD, 80000);
        List<OwStatDefDto> statDefs = statDefMapper.selectList(new LambdaQueryWrapper<OwStatDef>()
                        .eq(OwStatDef::getEnabled, true)
                        .orderByAsc(OwStatDef::getSortOrder)
                        .orderByAsc(OwStatDef::getId))
                .stream()
                .map(OwStatDefDto::from)
                .toList();
        return new OwBaseInfoDto(initialGold, statDefs);
    }

    public List<OwHeroSummaryDto> heroes() {
        return heroMapper.selectList(new LambdaQueryWrapper<OwHero>()
                        .eq(OwHero::getEnabled, true)
                        .orderByAsc(OwHero::getSortOrder)
                        .orderByAsc(OwHero::getId))
                .stream()
                .map(OwHeroSummaryDto::from)
                .toList();
    }

    public OwHeroDetailDto heroDetail(String heroCode) {
        OwHero hero = heroMapper.selectOne(new LambdaQueryWrapper<OwHero>()
                .eq(OwHero::getHeroCode, heroCode)
                .eq(OwHero::getEnabled, true)
                .last("LIMIT 1"));
        if (hero == null) {
            throw new IllegalArgumentException("Hero not found");
        }
        Integer initialGold = hero.getInitialGold() != null ? hero.getInitialGold() : readConfigInt(CONFIG_INITIAL_GOLD, 80000);
        return new OwHeroDetailDto(
                hero.getHeroCode(),
                hero.getHeroName(),
                hero.getDescription(),
                hero.getAvatarKey(),
                hero.getAvatarUrl(),
                initialGold,
                readIntMap(hero.getBaseStatsJson())
        );
    }

    public List<OwItemDto> items(String heroCode, String category, String quality) {
        // Ensure hero exists/enabled, so we can fail fast instead of returning all-global silently.
        if (heroMapper.selectOne(new LambdaQueryWrapper<OwHero>()
                .select(OwHero::getId)
                .eq(OwHero::getHeroCode, heroCode)
                .eq(OwHero::getEnabled, true)
                .last("LIMIT 1")) == null) {
            throw new IllegalArgumentException("Hero not found");
        }

        List<OwItem> items = itemMapper.selectEnabledItemsForHero(heroCode, category, quality);
        return items.stream().map(this::toItemDto).toList();
    }

    private OwItemDto toItemDto(OwItem e) {
        return new OwItemDto(
                e.getItemCode(),
                e.getItemName(),
                e.getPrice(),
                e.getQuality(),
                e.getCategory(),
                e.getImgKey(),
                e.getImgUrl(),
                readIntMap(e.getStatsJson()),
                e.getIsGlobal()
        );
    }

    private Integer readConfigInt(String key, int defaultValue) {
        OwConfig cfg = configMapper.selectById(key);
        if (cfg == null || cfg.getConfigValue() == null || cfg.getConfigValue().isBlank()) {
            return defaultValue;
        }
        try {
            Map<String, Object> obj = objectMapper.readValue(cfg.getConfigValue(), new TypeReference<>() {
            });
            Object val = obj.get("value");
            if (val instanceof Number n) {
                return n.intValue();
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Map<String, Integer> readIntMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}

