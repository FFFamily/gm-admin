package com.rcszh.gm.ow.service.lfg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.lfg.OwLfgMemberDto;
import com.rcszh.gm.ow.dto.lfg.OwLfgTeamInviteDto;
import com.rcszh.gm.ow.dto.lfg.OwLfgTeamSummaryDto;
import com.rcszh.gm.ow.entity.OwLfgTeam;
import com.rcszh.gm.ow.entity.OwLfgTeamMember;
import com.rcszh.gm.ow.mapper.OwLfgTeamMapper;
import com.rcszh.gm.ow.mapper.OwLfgTeamMemberMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OwLfgQueryService {

    private final OwLfgTeamMapper teamMapper;
    private final OwLfgTeamMemberMapper memberMapper;
    private final ObjectMapper objectMapper;

    public OwLfgQueryService(OwLfgTeamMapper teamMapper,
                             OwLfgTeamMemberMapper memberMapper,
                             ObjectMapper objectMapper) {
        this.teamMapper = teamMapper;
        this.memberMapper = memberMapper;
        this.objectMapper = objectMapper;
    }

    public PageResult<OwLfgTeamSummaryDto> list(long page,
                                               long size,
                                               String keyword,
                                               String modeCode,
                                               String platformCode,
                                               Integer voiceRequired,
                                               String status,
                                               String needRole) {
        LocalDateTime now = LocalDateTime.now();

        var w = new LambdaQueryWrapper<OwLfgTeam>();
        String st = (status == null || status.isBlank()) ? "OPEN" : status.trim().toUpperCase(Locale.ROOT);
        w.eq(OwLfgTeam::getStatus, st);

        // Default listing only returns non-expired teams.
        if ("OPEN".equals(st) || "CLOSED".equals(st)) {
            w.gt(OwLfgTeam::getExpiresAt, now);
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            w.and(x -> x.like(OwLfgTeam::getTitle, kw).or().like(OwLfgTeam::getNote, kw));
        }
        if (modeCode != null && !modeCode.isBlank()) {
            w.eq(OwLfgTeam::getModeCode, modeCode.trim());
        }
        if (platformCode != null && !platformCode.isBlank()) {
            w.eq(OwLfgTeam::getPlatformCode, platformCode.trim());
        }
        if (voiceRequired != null) {
            w.eq(OwLfgTeam::getVoiceRequired, voiceRequired);
        }
        if (needRole != null && !needRole.isBlank()) {
            // JSON column: need_roles_json is an array like ["TANK","DPS"].
            // Use JSON_CONTAINS for filter. This keeps MVP paging simple without custom SQL.
            String role = needRole.trim().toUpperCase(Locale.ROOT);
            w.apply("JSON_CONTAINS(need_roles_json, JSON_QUOTE({0}))", role);
        }

        w.orderByDesc(OwLfgTeam::getCreatedAt).orderByDesc(OwLfgTeam::getId);

        Page<OwLfgTeam> p = teamMapper.selectPage(new Page<>(page, size), w);
        List<OwLfgTeamSummaryDto> records = p.getRecords().stream().map(r -> toSummary(r, now)).toList();
        return new PageResult<>(records, p.getTotal(), p.getCurrent(), p.getSize());
    }

    public OwLfgTeamInviteDto invite(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new IllegalArgumentException("inviteCode required");
        }
        OwLfgTeam team = teamMapper.selectByInviteCode(inviteCode.trim());
        if (team == null) {
            throw new IllegalArgumentException("Team not found");
        }

        LocalDateTime now = LocalDateTime.now();
        String status = normalizeStatus(team, now);
        List<OwLfgTeamMember> members = memberMapper.listJoined(team.getId());

        return new OwLfgTeamInviteDto(
                team.getId(),
                team.getInviteCode(),
                team.getTitle(),
                team.getModeCode(),
                team.getPlatformCode(),
                Boolean.TRUE.equals(team.getAllowCrossplay()),
                nvl(team.getCapacity()),
                nvl(team.getMemberCount()),
                Boolean.TRUE.equals(team.getAutoApprove()),
                nvl(team.getVoiceRequired()),
                team.getRegionCode(),
                team.getLanguageCode(),
                team.getRankMin(),
                team.getRankMax(),
                readStringList(team.getNeedRolesJson()),
                readStringList(team.getPreferredHeroCodesJson()),
                readStringList(team.getTagsJson()),
                team.getNote(),
                team.getCreatedByName(),
                status,
                team.getExpiresAt(),
                team.getCreatedAt(),
                members.stream().map(m -> toMemberDto(m, team.getCreatedByAccountId())).toList()
        );
    }

    private OwLfgTeamSummaryDto toSummary(OwLfgTeam r, LocalDateTime now) {
        return new OwLfgTeamSummaryDto(
                r.getId(),
                r.getInviteCode(),
                r.getTitle(),
                r.getModeCode(),
                r.getPlatformCode(),
                Boolean.TRUE.equals(r.getAllowCrossplay()),
                nvl(r.getCapacity()),
                nvl(r.getMemberCount()),
                Boolean.TRUE.equals(r.getAutoApprove()),
                nvl(r.getVoiceRequired()),
                r.getRegionCode(),
                r.getLanguageCode(),
                r.getRankMin(),
                r.getRankMax(),
                readStringList(r.getNeedRolesJson()),
                readStringList(r.getPreferredHeroCodesJson()),
                readStringList(r.getTagsJson()),
                r.getCreatedByName(),
                normalizeStatus(r, now),
                r.getExpiresAt(),
                r.getCreatedAt()
        );
    }

    private OwLfgMemberDto toMemberDto(OwLfgTeamMember m, Long leaderAccountId) {
        return new OwLfgMemberDto(
                m.getId(),
                m.getAccountId(),
                m.getDisplayName(),
                readStringList(m.getRoleTagsJson()),
                leaderAccountId != null && leaderAccountId.equals(m.getAccountId()),
                m.getJoinedAt()
        );
    }

    private String normalizeStatus(OwLfgTeam team, LocalDateTime now) {
        String st = team.getStatus() != null ? team.getStatus() : "OPEN";
        if (!"DISBANDED".equals(st) && team.getExpiresAt() != null && team.getExpiresAt().isBefore(now)) {
            return "EXPIRED";
        }
        int cap = nvl(team.getCapacity());
        int mc = nvl(team.getMemberCount());
        if ("OPEN".equals(st) && cap > 0 && mc >= cap) {
            return "FULL";
        }
        return st;
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<String> list = objectMapper.readValue(json, new TypeReference<>() {
            });
            return list.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static int nvl(Integer n) {
        return n != null ? n : 0;
    }
}
