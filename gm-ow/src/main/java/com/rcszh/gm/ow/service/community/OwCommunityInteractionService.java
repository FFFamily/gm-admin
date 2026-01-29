package com.rcszh.gm.ow.service.community;

import cn.dev33.satoken.stp.StpUtil;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.ow.dto.community.OwLoadoutInteractStateDto;
import com.rcszh.gm.ow.entity.OwLoadout;
import com.rcszh.gm.ow.mapper.OwLoadoutFavoriteMapper;
import com.rcszh.gm.ow.mapper.OwLoadoutLikeMapper;
import com.rcszh.gm.ow.mapper.OwLoadoutMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwCommunityInteractionService {

    private final OwLoadoutMapper loadoutMapper;
    private final OwLoadoutLikeMapper likeMapper;
    private final OwLoadoutFavoriteMapper favoriteMapper;

    public OwCommunityInteractionService(OwLoadoutMapper loadoutMapper,
                                         OwLoadoutLikeMapper likeMapper,
                                         OwLoadoutFavoriteMapper favoriteMapper) {
        this.loadoutMapper = loadoutMapper;
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
    }

    @Transactional
    public OwLoadoutInteractStateDto like(long loadoutId) {
        long accountId = requireAccountId();
        OwLoadout row = requirePublished(loadoutId);

        int inserted = likeMapper.insertIgnore(loadoutId, accountId);
        if (inserted > 0) {
            loadoutMapper.incLike(loadoutId);
            row.setLikeCount(nvl(row.getLikeCount()) + 1);
        }

        return new OwLoadoutInteractStateDto(
                loadoutId,
                nvl(row.getLikeCount()),
                nvl(row.getFavoriteCount()),
                true,
                favoriteMapper.exists(loadoutId, accountId) > 0
        );
    }

    @Transactional
    public OwLoadoutInteractStateDto unlike(long loadoutId) {
        long accountId = requireAccountId();
        OwLoadout row = requirePublished(loadoutId);

        int deleted = likeMapper.deleteOne(loadoutId, accountId);
        if (deleted > 0) {
            loadoutMapper.decLike(loadoutId);
            row.setLikeCount(Math.max(0, nvl(row.getLikeCount()) - 1));
        }

        return new OwLoadoutInteractStateDto(
                loadoutId,
                nvl(row.getLikeCount()),
                nvl(row.getFavoriteCount()),
                false,
                favoriteMapper.exists(loadoutId, accountId) > 0
        );
    }

    @Transactional
    public OwLoadoutInteractStateDto favorite(long loadoutId) {
        long accountId = requireAccountId();
        OwLoadout row = requirePublished(loadoutId);

        int inserted = favoriteMapper.insertIgnore(loadoutId, accountId);
        if (inserted > 0) {
            loadoutMapper.incFavorite(loadoutId);
            row.setFavoriteCount(nvl(row.getFavoriteCount()) + 1);
        }

        return new OwLoadoutInteractStateDto(
                loadoutId,
                nvl(row.getLikeCount()),
                nvl(row.getFavoriteCount()),
                likeMapper.exists(loadoutId, accountId) > 0,
                true
        );
    }

    @Transactional
    public OwLoadoutInteractStateDto unfavorite(long loadoutId) {
        long accountId = requireAccountId();
        OwLoadout row = requirePublished(loadoutId);

        int deleted = favoriteMapper.deleteOne(loadoutId, accountId);
        if (deleted > 0) {
            loadoutMapper.decFavorite(loadoutId);
            row.setFavoriteCount(Math.max(0, nvl(row.getFavoriteCount()) - 1));
        }

        return new OwLoadoutInteractStateDto(
                loadoutId,
                nvl(row.getLikeCount()),
                nvl(row.getFavoriteCount()),
                likeMapper.exists(loadoutId, accountId) > 0,
                false
        );
    }

    public OwLoadoutInteractStateDto myState(long loadoutId) {
        long accountId = requireAccountId();
        OwLoadout row = requirePublished(loadoutId);
        boolean liked = likeMapper.exists(loadoutId, accountId) > 0;
        boolean favorited = favoriteMapper.exists(loadoutId, accountId) > 0;
        return new OwLoadoutInteractStateDto(
                loadoutId,
                nvl(row.getLikeCount()),
                nvl(row.getFavoriteCount()),
                liked,
                favorited
        );
    }

    private long requireAccountId() {
        Long accountId = LoginIdUtils.parseAccountId(StpUtil.getLoginId());
        if (accountId == null) {
            throw new IllegalArgumentException("Account login required");
        }
        return accountId;
    }

    private OwLoadout requirePublished(long loadoutId) {
        OwLoadout row = loadoutMapper.selectById(loadoutId);
        if (row == null || !"PUBLISHED".equalsIgnoreCase(row.getStatus())) {
            throw new IllegalArgumentException("Loadout not found");
        }
        return row;
    }

    private static int nvl(Integer i) {
        return i == null ? 0 : i;
    }
}
