package com.rcszh.gm.ow.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.admin.OwAdminLoadoutDetailDto;
import com.rcszh.gm.ow.dto.admin.OwAdminLoadoutDto;
import com.rcszh.gm.ow.entity.OwHero;
import com.rcszh.gm.ow.entity.OwLoadout;
import com.rcszh.gm.ow.mapper.OwHeroMapper;
import com.rcszh.gm.ow.mapper.OwLoadoutMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OwLoadoutAdminService {

    private final OwLoadoutMapper loadoutMapper;
    private final OwHeroMapper heroMapper;
    private final AuditLogService auditLogService;

    public OwLoadoutAdminService(OwLoadoutMapper loadoutMapper, OwHeroMapper heroMapper, AuditLogService auditLogService) {
        this.loadoutMapper = loadoutMapper;
        this.heroMapper = heroMapper;
        this.auditLogService = auditLogService;
    }

    public PageResult<OwAdminLoadoutDto> list(long page,
                                             long size,
                                             String keyword,
                                             String heroCode,
                                             String status,
                                             Boolean featured,
                                             Boolean pinned) {
        var wrapper = new LambdaQueryWrapper<OwLoadout>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(OwLoadout::getTitle, kw)
                    .or().like(OwLoadout::getDescription, kw)
                    .or().like(OwLoadout::getCreatedByName, kw));
        }
        if (heroCode != null && !heroCode.isBlank()) {
            wrapper.eq(OwLoadout::getHeroCode, heroCode.trim());
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(OwLoadout::getStatus, status.trim().toUpperCase(Locale.ROOT));
        }
        if (featured != null) {
            wrapper.eq(OwLoadout::getIsFeatured, featured);
        }
        if (pinned != null) {
            wrapper.eq(OwLoadout::getIsPinned, pinned);
        }

        wrapper.orderByDesc(OwLoadout::getIsPinned)
                .orderByDesc(OwLoadout::getPinnedAt)
                .orderByDesc(OwLoadout::getIsFeatured)
                .orderByDesc(OwLoadout::getFeaturedAt)
                .orderByDesc(OwLoadout::getId);

        Page<OwLoadout> p = loadoutMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, String> heroNameMap = loadHeroNames(p.getRecords());
        var records = p.getRecords().stream().map(r -> new OwAdminLoadoutDto(
                r.getId(),
                r.getHeroCode(),
                heroNameMap.getOrDefault(r.getHeroCode(), r.getHeroCode()),
                r.getTitle(),
                r.getCreatedByName(),
                r.getStatus(),
                nvl(r.getViewCount()),
                nvl(r.getLikeCount()),
                nvl(r.getFavoriteCount()),
                Boolean.TRUE.equals(r.getIsFeatured()),
                Boolean.TRUE.equals(r.getIsPinned()),
                r.getCreatedAt()
        )).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }

    public OwAdminLoadoutDetailDto detail(long id) {
        OwLoadout r = loadoutMapper.selectById(id);
        if (r == null) {
            throw new IllegalArgumentException("Loadout not found");
        }
        String heroName = readHeroName(r.getHeroCode());
        return new OwAdminLoadoutDetailDto(
                r.getId(),
                r.getHeroCode(),
                heroName,
                r.getTitle(),
                r.getDescription(),
                r.getItemsJson(),
                r.getCreatedByAccountId(),
                r.getCreatedByName(),
                r.getStatus(),
                nvl(r.getViewCount()),
                nvl(r.getLikeCount()),
                nvl(r.getFavoriteCount()),
                Boolean.TRUE.equals(r.getIsFeatured()),
                Boolean.TRUE.equals(r.getIsPinned()),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    @Transactional
    public OwAdminLoadoutDetailDto setStatus(long id, String status) {
        String s = status.trim().toUpperCase(Locale.ROOT);
        if (!"PUBLISHED".equals(s) && !"HIDDEN".equals(s)) {
            throw new IllegalArgumentException("Invalid status");
        }
        if (loadoutMapper.selectById(id) == null) {
            throw new IllegalArgumentException("Loadout not found");
        }
        loadoutMapper.setStatus(id, s);
        auditLogService.success("OW_LOADOUT_STATUS", "OW_LOADOUT", String.valueOf(id), Map.of("status", s));
        return detail(id);
    }

    @Transactional
    public OwAdminLoadoutDetailDto setFeatured(long id, boolean featured) {
        if (loadoutMapper.selectById(id) == null) {
            throw new IllegalArgumentException("Loadout not found");
        }
        loadoutMapper.setFeatured(id, featured);
        auditLogService.success("OW_LOADOUT_FEATURED", "OW_LOADOUT", String.valueOf(id), Map.of("featured", featured));
        return detail(id);
    }

    @Transactional
    public OwAdminLoadoutDetailDto setPinned(long id, boolean pinned) {
        if (loadoutMapper.selectById(id) == null) {
            throw new IllegalArgumentException("Loadout not found");
        }
        loadoutMapper.setPinned(id, pinned);
        auditLogService.success("OW_LOADOUT_PINNED", "OW_LOADOUT", String.valueOf(id), Map.of("pinned", pinned));
        return detail(id);
    }

    private Map<String, String> loadHeroNames(List<OwLoadout> rows) {
        var codes = rows.stream().map(OwLoadout::getHeroCode).filter(c -> c != null && !c.isBlank()).distinct().toList();
        if (codes.isEmpty()) return Map.of();

        var heroes = heroMapper.selectList(new LambdaQueryWrapper<OwHero>()
                .select(OwHero::getHeroCode, OwHero::getHeroName)
                .in(OwHero::getHeroCode, codes));
        Map<String, String> map = new HashMap<>();
        for (var h : heroes) {
            map.put(h.getHeroCode(), h.getHeroName());
        }
        return map;
    }

    private String readHeroName(String heroCode) {
        if (heroCode == null) return null;
        OwHero h = heroMapper.selectOne(new LambdaQueryWrapper<OwHero>()
                .select(OwHero::getHeroName)
                .eq(OwHero::getHeroCode, heroCode)
                .last("LIMIT 1"));
        return h != null && h.getHeroName() != null ? h.getHeroName() : heroCode;
    }

    private static int nvl(Integer i) {
        return i == null ? 0 : i;
    }
}

