package com.rcszh.gm.ow.service.community;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.ow.dto.community.OwLoadoutCreateRequest;
import com.rcszh.gm.ow.dto.community.OwLoadoutCreateResponse;
import com.rcszh.gm.ow.entity.OwHero;
import com.rcszh.gm.ow.entity.OwItem;
import com.rcszh.gm.ow.entity.OwLoadout;
import com.rcszh.gm.ow.mapper.OwHeroMapper;
import com.rcszh.gm.ow.mapper.OwItemMapper;
import com.rcszh.gm.ow.mapper.OwLoadoutMapper;
import com.rcszh.gm.user.entity.AppAccount;
import com.rcszh.gm.user.mapper.AppAccountMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OwCommunityWriteService {

    private final OwLoadoutMapper loadoutMapper;
    private final OwHeroMapper heroMapper;
    private final OwItemMapper itemMapper;
    private final AppAccountMapper accountMapper;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public OwCommunityWriteService(OwLoadoutMapper loadoutMapper,
                                   OwHeroMapper heroMapper,
                                   OwItemMapper itemMapper,
                                   AppAccountMapper accountMapper,
                                   ObjectMapper objectMapper,
                                   AuditLogService auditLogService) {
        this.loadoutMapper = loadoutMapper;
        this.heroMapper = heroMapper;
        this.itemMapper = itemMapper;
        this.accountMapper = accountMapper;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public OwLoadoutCreateResponse create(OwLoadoutCreateRequest req) {
        Long accountId = LoginIdUtils.parseAccountId(StpUtil.getLoginId());
        if (accountId == null) {
            throw new IllegalArgumentException("Account login required");
        }

        AppAccount acc = accountMapper.selectById(accountId);
        if (acc == null || !Boolean.TRUE.equals(acc.getEnabled())) {
            throw new IllegalArgumentException("Account not found");
        }

        String heroCode = req.heroCode().trim();
        OwHero hero = heroMapper.selectOne(new LambdaQueryWrapper<OwHero>()
                .select(OwHero::getId)
                .eq(OwHero::getHeroCode, heroCode)
                .eq(OwHero::getEnabled, true)
                .last("LIMIT 1"));
        if (hero == null) {
            throw new IllegalArgumentException("Hero not found");
        }

        List<String> itemCodes = normalizeItemCodes(req.itemCodes());
        if (itemCodes.isEmpty()) {
            throw new IllegalArgumentException("items required");
        }
        if (itemCodes.size() > 6) {
            throw new IllegalArgumentException("items max 6");
        }

        // Validate item availability for this hero: enabled + (global OR bound to hero)
        List<OwItem> allowed = itemMapper.selectEnabledItemsForHero(heroCode, null, null);
        Set<String> allowedCodes = new HashSet<>();
        for (var i : allowed) {
            allowedCodes.add(i.getItemCode());
        }
        for (String code : itemCodes) {
            if (!allowedCodes.contains(code)) {
                throw new IllegalArgumentException("Item not available for hero: " + code);
            }
        }

        String authorName = (acc.getDisplayName() != null && !acc.getDisplayName().isBlank())
                ? acc.getDisplayName().trim()
                : acc.getUsername();

        var row = new OwLoadout();
        row.setHeroCode(heroCode);
        row.setTitle(req.title().trim());
        row.setDescription(trimToNull(req.description()));
        row.setItemsJson(toJson(itemCodes));
        row.setCreatedByAccountId(accountId);
        row.setCreatedByName(authorName);
        row.setStatus("PUBLISHED");
        row.setViewCount(0);
        row.setLikeCount(0);
        row.setFavoriteCount(0);
        row.setIsFeatured(false);
        row.setFeaturedAt(null);
        row.setIsPinned(false);
        row.setPinnedAt(null);
        loadoutMapper.insert(row);

        auditLogService.success("OW_LOADOUT_CREATE", "OW_LOADOUT", String.valueOf(row.getId()), Map.of(
                "heroCode", heroCode,
                "title", row.getTitle()
        ));

        return new OwLoadoutCreateResponse(row.getId());
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static List<String> normalizeItemCodes(List<String> codes) {
        if (codes == null) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String c : codes) {
            if (c == null) continue;
            String t = c.trim();
            if (t.isEmpty()) continue;
            set.add(t);
        }
        return set.stream().toList();
    }

    private String toJson(List<String> codes) {
        try {
            return objectMapper.writeValueAsString(codes);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid items");
        }
    }
}
