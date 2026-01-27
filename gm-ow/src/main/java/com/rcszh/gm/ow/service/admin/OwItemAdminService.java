package com.rcszh.gm.ow.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.admin.*;
import com.rcszh.gm.ow.entity.OwHero;
import com.rcszh.gm.ow.entity.OwHeroItem;
import com.rcszh.gm.ow.entity.OwItem;
import com.rcszh.gm.ow.mapper.OwHeroItemMapper;
import com.rcszh.gm.ow.mapper.OwHeroMapper;
import com.rcszh.gm.ow.mapper.OwItemMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OwItemAdminService {

    private final OwItemMapper itemMapper;
    private final OwHeroMapper heroMapper;
    private final OwHeroItemMapper heroItemMapper;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public OwItemAdminService(OwItemMapper itemMapper,
                              OwHeroMapper heroMapper,
                              OwHeroItemMapper heroItemMapper,
                              ObjectMapper objectMapper,
                              AuditLogService auditLogService) {
        this.itemMapper = itemMapper;
        this.heroMapper = heroMapper;
        this.heroItemMapper = heroItemMapper;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    public PageResult<OwAdminItemDto> list(long page, long size, String keyword, String category, String quality, Boolean enabled, Boolean isGlobal) {
        var wrapper = new LambdaQueryWrapper<OwItem>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(OwItem::getItemCode, kw).or().like(OwItem::getItemName, kw));
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(OwItem::getCategory, category.trim());
        }
        if (quality != null && !quality.isBlank()) {
            wrapper.eq(OwItem::getQuality, quality.trim());
        }
        if (enabled != null) {
            wrapper.eq(OwItem::getEnabled, enabled);
        }
        if (isGlobal != null) {
            wrapper.eq(OwItem::getIsGlobal, isGlobal);
        }
        wrapper.orderByAsc(OwItem::getSortOrder).orderByAsc(OwItem::getId);

        Page<OwItem> p = itemMapper.selectPage(new Page<>(page, size), wrapper);
        var records = p.getRecords().stream().map(OwAdminItemDto::from).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }

    public OwAdminItemDetailDto detail(long id) {
        OwItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }
        return toDetail(item);
    }

    @Transactional
    public OwAdminItemDetailDto create(OwItemCreateRequest req) {
        String itemCode = req.itemCode().trim();
        if (itemMapper.selectOne(new LambdaQueryWrapper<OwItem>().eq(OwItem::getItemCode, itemCode).last("LIMIT 1")) != null) {
            throw new IllegalStateException("Item code already exists");
        }

        var item = new OwItem();
        item.setItemCode(itemCode);
        item.setItemName(req.itemName().trim());
        item.setPrice(req.price());
        item.setQuality(req.quality().trim());
        item.setCategory(req.category().trim());
        item.setImgKey(trimToNull(req.imgKey()));
        item.setImgUrl(trimToNull(req.imgUrl()));
        item.setStatsJson(toJson(req.stats()));
        item.setIsGlobal(req.isGlobal());
        item.setEnabled(req.enabled());
        item.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        itemMapper.insert(item);

        replaceItemHeroes(item.getId(), item.getIsGlobal(), req.heroIds());

        auditLogService.success("OW_ITEM_CREATE", "OW_ITEM", String.valueOf(item.getId()), req);
        return detail(item.getId());
    }

    @Transactional
    public OwAdminItemDetailDto update(long id, OwItemUpdateRequest req) {
        OwItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }

        item.setItemName(req.itemName().trim());
        item.setPrice(req.price());
        item.setQuality(req.quality().trim());
        item.setCategory(req.category().trim());
        item.setImgKey(trimToNull(req.imgKey()));
        item.setImgUrl(trimToNull(req.imgUrl()));
        item.setStatsJson(toJson(req.stats()));
        item.setIsGlobal(req.isGlobal());
        item.setEnabled(req.enabled());
        item.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        itemMapper.updateById(item);

        replaceItemHeroes(item.getId(), item.getIsGlobal(), req.heroIds());

        auditLogService.success("OW_ITEM_UPDATE", "OW_ITEM", String.valueOf(item.getId()), req);
        return detail(item.getId());
    }

    @Transactional
    public void delete(long id) {
        OwItem item = itemMapper.selectById(id);
        if (item == null) {
            return;
        }
        itemMapper.deleteById(id);
        auditLogService.success("OW_ITEM_DELETE", "OW_ITEM", String.valueOf(id), null);
    }

    @Transactional
    public OwAdminItemDetailDto bindHeroes(long itemId, OwItemBindHeroesRequest req) {
        OwItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }
        if (Boolean.TRUE.equals(item.getIsGlobal())) {
            throw new IllegalArgumentException("Global item does not need hero binding");
        }
        replaceItemHeroes(itemId, false, req.heroIds());
        auditLogService.success("OW_ITEM_BIND_HEROES", "OW_ITEM", String.valueOf(itemId), req);
        return detail(itemId);
    }

    private OwAdminItemDetailDto toDetail(OwItem item) {
        List<Long> heroIds = heroItemMapper.selectHeroIdsByItemId(item.getId());
        return new OwAdminItemDetailDto(
                item.getId(),
                item.getItemCode(),
                item.getItemName(),
                item.getPrice(),
                item.getQuality(),
                item.getCategory(),
                item.getImgKey(),
                item.getImgUrl(),
                readIntMap(item.getStatsJson()),
                item.getIsGlobal(),
                heroIds,
                item.getEnabled(),
                item.getSortOrder()
        );
    }

    private void replaceItemHeroes(Long itemId, boolean isGlobal, List<Long> heroIds) {
        heroItemMapper.delete(new LambdaQueryWrapper<OwHeroItem>().eq(OwHeroItem::getItemId, itemId));
        if (isGlobal) {
            return;
        }
        if (heroIds == null || heroIds.isEmpty()) {
            return;
        }

        long cnt = heroMapper.selectCount(new LambdaQueryWrapper<OwHero>().in(OwHero::getId, heroIds));
        if (cnt != heroIds.size()) {
            throw new IllegalArgumentException("Hero not found");
        }

        for (Long heroId : heroIds) {
            var hi = new OwHeroItem();
            hi.setHeroId(heroId);
            hi.setItemId(itemId);
            heroItemMapper.insert(hi);
        }
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
            throw new IllegalArgumentException("Invalid stats");
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

