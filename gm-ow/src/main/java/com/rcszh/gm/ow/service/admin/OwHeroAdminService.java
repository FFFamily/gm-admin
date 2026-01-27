package com.rcszh.gm.ow.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.admin.*;
import com.rcszh.gm.ow.entity.OwHero;
import com.rcszh.gm.ow.mapper.OwHeroMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
public class OwHeroAdminService {

    private final OwHeroMapper heroMapper;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public OwHeroAdminService(OwHeroMapper heroMapper, ObjectMapper objectMapper, AuditLogService auditLogService) {
        this.heroMapper = heroMapper;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    public PageResult<OwAdminHeroDto> list(long page, long size, String keyword, Boolean enabled) {
        var wrapper = new LambdaQueryWrapper<OwHero>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(OwHero::getHeroCode, kw).or().like(OwHero::getHeroName, kw));
        }
        if (enabled != null) {
            wrapper.eq(OwHero::getEnabled, enabled);
        }
        wrapper.orderByAsc(OwHero::getSortOrder).orderByAsc(OwHero::getId);

        Page<OwHero> p = heroMapper.selectPage(new Page<>(page, size), wrapper);
        var records = p.getRecords().stream().map(OwAdminHeroDto::from).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }

    public OwAdminHeroDetailDto detail(long id) {
        OwHero hero = heroMapper.selectById(id);
        if (hero == null) {
            throw new IllegalArgumentException("Hero not found");
        }
        return toDetail(hero);
    }

    @Transactional
    public OwAdminHeroDetailDto create(OwHeroCreateRequest req) {
        String heroCode = req.heroCode().trim();
        if (heroMapper.selectOne(new LambdaQueryWrapper<OwHero>().eq(OwHero::getHeroCode, heroCode).last("LIMIT 1")) != null) {
            throw new IllegalStateException("Hero code already exists");
        }

        var hero = new OwHero();
        hero.setHeroCode(heroCode);
        hero.setHeroName(req.heroName().trim());
        hero.setDescription(trimToNull(req.description()));
        hero.setAvatarKey(trimToNull(req.avatarKey()));
        hero.setAvatarUrl(trimToNull(req.avatarUrl()));
        hero.setInitialGold(req.initialGold());
        hero.setBaseStatsJson(toJson(req.baseStats()));
        hero.setEnabled(req.enabled());
        hero.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        heroMapper.insert(hero);

        auditLogService.success("OW_HERO_CREATE", "OW_HERO", String.valueOf(hero.getId()), req);
        return detail(hero.getId());
    }

    @Transactional
    public OwAdminHeroDetailDto update(long id, OwHeroUpdateRequest req) {
        OwHero hero = heroMapper.selectById(id);
        if (hero == null) {
            throw new IllegalArgumentException("Hero not found");
        }

        hero.setHeroName(req.heroName().trim());
        hero.setDescription(trimToNull(req.description()));
        hero.setAvatarKey(trimToNull(req.avatarKey()));
        hero.setAvatarUrl(trimToNull(req.avatarUrl()));
        hero.setInitialGold(req.initialGold());
        hero.setBaseStatsJson(toJson(req.baseStats()));
        hero.setEnabled(req.enabled());
        hero.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        heroMapper.updateById(hero);

        auditLogService.success("OW_HERO_UPDATE", "OW_HERO", String.valueOf(hero.getId()), req);
        return detail(hero.getId());
    }

    @Transactional
    public void delete(long id) {
        OwHero hero = heroMapper.selectById(id);
        if (hero == null) {
            return;
        }
        heroMapper.deleteById(id);
        auditLogService.success("OW_HERO_DELETE", "OW_HERO", String.valueOf(id), null);
    }

    private OwAdminHeroDetailDto toDetail(OwHero hero) {
        return new OwAdminHeroDetailDto(
                hero.getId(),
                hero.getHeroCode(),
                hero.getHeroName(),
                hero.getDescription(),
                hero.getAvatarKey(),
                hero.getAvatarUrl(),
                hero.getInitialGold(),
                readIntMap(hero.getBaseStatsJson()),
                hero.getEnabled(),
                hero.getSortOrder()
        );
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String toJson(Map<String, Integer> map) {
        Map<String, Integer> safe = map != null ? map : Collections.emptyMap();
        try {
            return objectMapper.writeValueAsString(safe);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid baseStats");
        }
    }

    private Map<String, Integer> readIntMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Integer.class));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}

