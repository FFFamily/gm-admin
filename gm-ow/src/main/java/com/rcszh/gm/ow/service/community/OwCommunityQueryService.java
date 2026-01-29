package com.rcszh.gm.ow.service.community;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.ow.dto.OwItemDto;
import com.rcszh.gm.ow.dto.community.OwCommunityHeroStatDto;
import com.rcszh.gm.ow.dto.community.OwHeroLoadoutCountRow;
import com.rcszh.gm.ow.dto.community.OwLoadoutCategoryCountsDto;
import com.rcszh.gm.ow.dto.community.OwLoadoutDetailDto;
import com.rcszh.gm.ow.dto.community.OwLoadoutItemPreviewDto;
import com.rcszh.gm.ow.dto.community.OwLoadoutSummaryDto;
import com.rcszh.gm.ow.dto.community.OwLoadoutTopStatDto;
import com.rcszh.gm.ow.entity.OwHero;
import com.rcszh.gm.ow.entity.OwItem;
import com.rcszh.gm.ow.entity.OwLoadout;
import com.rcszh.gm.ow.mapper.OwHeroMapper;
import com.rcszh.gm.ow.mapper.OwItemMapper;
import com.rcszh.gm.ow.mapper.OwLoadoutFavoriteMapper;
import com.rcszh.gm.ow.mapper.OwLoadoutLikeMapper;
import com.rcszh.gm.ow.mapper.OwLoadoutMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OwCommunityQueryService {

    private final OwLoadoutMapper loadoutMapper;
    private final OwHeroMapper heroMapper;
    private final OwItemMapper itemMapper;
    private final OwLoadoutLikeMapper likeMapper;
    private final OwLoadoutFavoriteMapper favoriteMapper;
    private final ObjectMapper objectMapper;

    public OwCommunityQueryService(OwLoadoutMapper loadoutMapper,
                                   OwHeroMapper heroMapper,
                                   OwItemMapper itemMapper,
                                   OwLoadoutLikeMapper likeMapper,
                                   OwLoadoutFavoriteMapper favoriteMapper,
                                   ObjectMapper objectMapper) {
        this.loadoutMapper = loadoutMapper;
        this.heroMapper = heroMapper;
        this.itemMapper = itemMapper;
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
        this.objectMapper = objectMapper;
    }

    public PageResult<OwLoadoutSummaryDto> list(long page, long size, String heroCode, String keyword, String sort, Boolean featuredOnly) {
        var wrapper = new LambdaQueryWrapper<OwLoadout>();
        wrapper.eq(OwLoadout::getStatus, "PUBLISHED");

        if (Boolean.TRUE.equals(featuredOnly)) {
            wrapper.eq(OwLoadout::getIsFeatured, true);
        }
        if (heroCode != null && !heroCode.isBlank()) {
            wrapper.eq(OwLoadout::getHeroCode, heroCode.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(OwLoadout::getTitle, kw).or().like(OwLoadout::getDescription, kw));
        }

        // Global ordering: pinned/featured first, then by selected sort.
        wrapper.orderByDesc(OwLoadout::getIsPinned)
                .orderByDesc(OwLoadout::getPinnedAt)
                .orderByDesc(OwLoadout::getIsFeatured)
                .orderByDesc(OwLoadout::getFeaturedAt);

        String s = (sort == null || sort.isBlank()) ? "newest" : sort.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            case "hot" -> wrapper.orderByDesc(OwLoadout::getViewCount)
                    .orderByDesc(OwLoadout::getLikeCount)
                    .orderByDesc(OwLoadout::getFavoriteCount)
                    .orderByDesc(OwLoadout::getId);
            case "likes" -> wrapper.orderByDesc(OwLoadout::getLikeCount).orderByDesc(OwLoadout::getId);
            case "favorites" -> wrapper.orderByDesc(OwLoadout::getFavoriteCount).orderByDesc(OwLoadout::getId);
            case "views" -> wrapper.orderByDesc(OwLoadout::getViewCount).orderByDesc(OwLoadout::getId);
            default -> wrapper.orderByDesc(OwLoadout::getId);
        }

        Page<OwLoadout> p = loadoutMapper.selectPage(new Page<>(page, size), wrapper);
        List<OwLoadout> rows = p.getRecords();

        Map<String, String> heroNameMap = loadHeroNames(rows);

        Map<String, OwItem> itemMap = loadAllItems(rows);
        var records = rows.stream().map(r -> {
            List<String> itemCodes = readItemCodes(r.getItemsJson());
            List<OwLoadoutItemPreviewDto> previews = buildPreviews(itemCodes, itemMap);
            int totalPrice = previews.stream().mapToInt(i -> i.price() != null ? i.price() : 0).sum();
            Map<String, Integer> statTotals = sumStatsFromItems(itemCodes, itemMap);

            return new OwLoadoutSummaryDto(
                    r.getId(),
                    r.getHeroCode(),
                    heroNameMap.getOrDefault(r.getHeroCode(), r.getHeroCode()),
                    r.getTitle(),
                    r.getDescription(),
                    r.getCreatedByName(),
                    r.getCreatedAt(),
                    nvl(r.getViewCount()),
                    nvl(r.getLikeCount()),
                    nvl(r.getFavoriteCount()),
                    Boolean.TRUE.equals(r.getIsFeatured()),
                    Boolean.TRUE.equals(r.getIsPinned()),
                    totalPrice,
                    statTotals,
                    topStats(statTotals, 3),
                    categoryCounts(previews),
                    previews
            );
        }).toList();

        return new PageResult<>(records, p.getTotal(), page, size);
    }

    public OwLoadoutDetailDto detail(long id) {
        OwLoadout r = loadoutMapper.selectById(id);
        if (r == null || !"PUBLISHED".equalsIgnoreCase(r.getStatus())) {
            throw new IllegalArgumentException("Loadout not found");
        }

        // Increment view count (best-effort). Keep response viewCount consistent for this request.
        try {
            int affected = loadoutMapper.incView(id);
            if (affected > 0) {
                r.setViewCount(nvl(r.getViewCount()) + 1);
            }
        } catch (Exception ignored) {
        }

        String heroName = readHeroName(r.getHeroCode());
        List<String> itemCodes = readItemCodes(r.getItemsJson());
        List<OwItemDto> items = loadItems(itemCodes);

        int totalPrice = items.stream().mapToInt(i -> i.price() != null ? i.price() : 0).sum();
        Map<String, Integer> statTotals = sumStats(items);

        Long accountId = tryGetAccountId();
        boolean liked = false;
        boolean favorited = false;
        if (accountId != null) {
            liked = likeMapper.exists(id, accountId) > 0;
            favorited = favoriteMapper.exists(id, accountId) > 0;
        }

        return new OwLoadoutDetailDto(
                r.getId(),
                r.getHeroCode(),
                heroName,
                r.getTitle(),
                r.getDescription(),
                r.getCreatedByName(),
                r.getCreatedAt(),
                nvl(r.getViewCount()),
                nvl(r.getLikeCount()),
                nvl(r.getFavoriteCount()),
                Boolean.TRUE.equals(r.getIsFeatured()),
                Boolean.TRUE.equals(r.getIsPinned()),
                liked,
                favorited,
                items,
                totalPrice,
                statTotals
        );
    }

    public List<OwCommunityHeroStatDto> heroStats() {
        // 1) Load enabled heroes for display.
        var heroes = heroMapper.selectList(new LambdaQueryWrapper<OwHero>()
                .select(OwHero::getHeroCode, OwHero::getHeroName, OwHero::getSortOrder)
                .eq(OwHero::getEnabled, true)
                .orderByAsc(OwHero::getSortOrder)
                .orderByAsc(OwHero::getId));

        // 2) Count published loadouts by heroCode.
        var counts = loadoutMapper.countPublishedByHero();
        Map<String, Long> countMap = new HashMap<>();
        for (OwHeroLoadoutCountRow r : counts) {
            if (r.heroCode() == null) continue;
            countMap.put(r.heroCode(), r.cnt() != null ? r.cnt() : 0L);
        }

        return heroes.stream().map(h -> new OwCommunityHeroStatDto(
                h.getHeroCode(),
                h.getHeroName(),
                countMap.getOrDefault(h.getHeroCode(), 0L)
        )).toList();
    }

    private Map<String, String> loadHeroNames(List<OwLoadout> rows) {
        Set<String> codes = new HashSet<>();
        for (var r : rows) {
            if (r.getHeroCode() != null) codes.add(r.getHeroCode());
        }
        if (codes.isEmpty()) return Collections.emptyMap();

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
        OwHero h = heroMapper.selectOne(new LambdaQueryWrapper<OwHero>()
                .select(OwHero::getHeroName)
                .eq(OwHero::getHeroCode, heroCode)
                .last("LIMIT 1"));
        return h != null && h.getHeroName() != null ? h.getHeroName() : heroCode;
    }

    private List<String> readItemCodes(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) return List.of();
        try {
            List<String> list = objectMapper.readValue(itemsJson, new TypeReference<>() {
            });
            // Keep order, remove blanks.
            return list.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<OwItemDto> loadItems(List<String> itemCodes) {
        if (itemCodes == null || itemCodes.isEmpty()) {
            return List.of();
        }
        var items = itemMapper.selectList(new LambdaQueryWrapper<OwItem>().in(OwItem::getItemCode, itemCodes));
        Map<String, OwItem> map = new HashMap<>();
        for (var i : items) {
            map.put(i.getItemCode(), i);
        }

        List<OwItemDto> ordered = new ArrayList<>();
        for (String code : itemCodes) {
            OwItem i = map.get(code);
            if (i == null) continue;
            ordered.add(new OwItemDto(
                    i.getItemCode(),
                    i.getItemName(),
                    i.getPrice(),
                    i.getQuality(),
                    i.getCategory(),
                    i.getImgKey(),
                    i.getImgUrl(),
                    readIntMap(i.getStatsJson()),
                    i.getIsGlobal()
            ));
        }
        return ordered;
    }

    private Map<String, Integer> sumStats(List<OwItemDto> items) {
        Map<String, Integer> totals = new HashMap<>();
        for (var item : items) {
            if (item.stats() == null) continue;
            for (var e : item.stats().entrySet()) {
                if (e.getKey() == null) continue;
                totals.merge(e.getKey(), e.getValue() != null ? e.getValue() : 0, Integer::sum);
            }
        }
        return totals;
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

    private static int nvl(Integer i) {
        return i == null ? 0 : i;
    }

    private Long tryGetAccountId() {
        try {
            if (!StpUtil.isLogin()) return null;
            return LoginIdUtils.parseAccountId(StpUtil.getLoginId());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, OwItem> loadAllItems(List<OwLoadout> rows) {
        Set<String> codes = new HashSet<>();
        for (var r : rows) {
            for (var c : readItemCodes(r.getItemsJson())) {
                codes.add(c);
            }
        }
        if (codes.isEmpty()) return Collections.emptyMap();

        var items = itemMapper.selectList(new LambdaQueryWrapper<OwItem>().in(OwItem::getItemCode, codes));
        Map<String, OwItem> map = new HashMap<>();
        for (var i : items) {
            map.put(i.getItemCode(), i);
        }
        return map;
    }

    private List<OwLoadoutItemPreviewDto> buildPreviews(List<String> itemCodes, Map<String, OwItem> itemMap) {
        if (itemCodes == null || itemCodes.isEmpty() || itemMap == null || itemMap.isEmpty()) {
            return List.of();
        }
        List<OwLoadoutItemPreviewDto> list = new ArrayList<>();
        for (String code : itemCodes) {
            OwItem it = itemMap.get(code);
            if (it == null) continue;
            list.add(new OwLoadoutItemPreviewDto(
                    it.getItemCode(),
                    it.getItemName(),
                    it.getPrice(),
                    it.getQuality(),
                    it.getCategory(),
                    it.getImgKey(),
                    it.getImgUrl()
            ));
        }
        return list;
    }

    private Map<String, Integer> sumStatsFromItems(List<String> itemCodes, Map<String, OwItem> itemMap) {
        if (itemCodes == null || itemCodes.isEmpty() || itemMap == null || itemMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> totals = new HashMap<>();
        for (String code : itemCodes) {
            OwItem it = itemMap.get(code);
            if (it == null) continue;
            Map<String, Integer> stats = readIntMap(it.getStatsJson());
            for (var e : stats.entrySet()) {
                if (e.getKey() == null) continue;
                totals.merge(e.getKey(), e.getValue() != null ? e.getValue() : 0, Integer::sum);
            }
        }
        return totals;
    }

    private List<OwLoadoutTopStatDto> topStats(Map<String, Integer> statTotals, int limit) {
        if (statTotals == null || statTotals.isEmpty() || limit <= 0) return List.of();
        return statTotals.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue() != 0)
                .sorted((a, b) -> Integer.compare(Math.abs(b.getValue()), Math.abs(a.getValue())))
                .limit(limit)
                .map(e -> new OwLoadoutTopStatDto(e.getKey(), e.getValue()))
                .toList();
    }

    private OwLoadoutCategoryCountsDto categoryCounts(List<OwLoadoutItemPreviewDto> previews) {
        int weapon = 0, skill = 0, survival = 0, device = 0;
        if (previews != null) {
            for (var it : previews) {
                if (it == null || it.category() == null) continue;
                switch (it.category()) {
                    case "weapon" -> weapon++;
                    case "skill" -> skill++;
                    case "survival" -> survival++;
                    case "device" -> device++;
                    default -> {
                    }
                }
            }
        }
        return new OwLoadoutCategoryCountsDto(weapon, skill, survival, device);
    }
}
