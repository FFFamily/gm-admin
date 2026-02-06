package com.rcszh.gm.ow.service.lfg;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.ow.dto.lfg.*;
import com.rcszh.gm.ow.entity.OwLfgReport;
import com.rcszh.gm.ow.entity.OwLfgTeam;
import com.rcszh.gm.ow.entity.OwLfgTeamJoinRequest;
import com.rcszh.gm.ow.entity.OwLfgTeamMember;
import com.rcszh.gm.ow.mapper.*;
import com.rcszh.gm.user.entity.AppAccount;
import com.rcszh.gm.user.mapper.AppAccountMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OwLfgAccountService {

    private static final Set<String> MODE_CODES = Set.of("COMPETITIVE", "QUICK_PLAY", "CUSTOM", "ARMORY");
    private static final Set<String> PLATFORM_CODES = Set.of("PC", "CONSOLE");
    private static final Set<String> ROLE_CODES = Set.of("TANK", "DPS", "SUPPORT");
    private static final List<String> RANK_ORDER = List.of(
            "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "TOP500"
    );

    private final OwLfgTeamMapper teamMapper;
    private final OwLfgTeamMemberMapper memberMapper;
    private final OwLfgTeamJoinRequestMapper joinRequestMapper;
    private final OwLfgReportMapper reportMapper;
    private final AppAccountMapper accountMapper;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;
    private final SecureRandom random = new SecureRandom();

    public OwLfgAccountService(OwLfgTeamMapper teamMapper,
                               OwLfgTeamMemberMapper memberMapper,
                               OwLfgTeamJoinRequestMapper joinRequestMapper,
                               OwLfgReportMapper reportMapper,
                               AppAccountMapper accountMapper,
                               ObjectMapper objectMapper,
                               AuditLogService auditLogService) {
        this.teamMapper = teamMapper;
        this.memberMapper = memberMapper;
        this.joinRequestMapper = joinRequestMapper;
        this.reportMapper = reportMapper;
        this.accountMapper = accountMapper;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public OwLfgTeamCreateResponse create(OwLfgTeamCreateRequest req) {
        AppAccount acc = requireAccount();

        validateCreateRequest(req);

        Long accountId = acc.getId();
        enforceCreateLimits(accountId);

        String authorName = displayNameSnapshot(acc);
        LocalDateTime now = LocalDateTime.now();

        OwLfgTeam row = new OwLfgTeam();
        row.setTitle(req.title().trim());
        row.setModeCode(req.modeCode().trim().toUpperCase(Locale.ROOT));
        row.setPlatformCode(req.platformCode().trim().toUpperCase(Locale.ROOT));
        row.setAllowCrossplay(Boolean.TRUE.equals(req.allowCrossplay()));
        row.setCapacity(req.capacity());
        row.setMemberCount(1);
        row.setAutoApprove(Boolean.TRUE.equals(req.autoApprove()));
        row.setVoiceRequired(req.voiceRequired());
        row.setRegionCode(trimToNull(req.regionCode()));
        row.setLanguageCode(trimToNull(req.languageCode()));
        row.setRankMin(trimToNull(upper(req.rankMin())));
        row.setRankMax(trimToNull(upper(req.rankMax())));
        row.setNeedRolesJson(toJson(normalizeEnumList(req.needRoles(), ROLE_CODES, 3)));
        row.setPreferredHeroCodesJson(toJson(normalizeList(req.preferredHeroCodes(), 3)));
        row.setTagsJson(toJson(normalizeList(req.tags(), 10)));
        row.setNote(trimToNull(req.note()));
        row.setContactJson(toJson(normalizeContact(req.contact())));
        row.setInviteCode(generateInviteCode());
        row.setStatus("OPEN");
        row.setExpiresAt(now.plusMinutes(req.durationMinutes()));
        row.setCreatedByAccountId(accountId);
        row.setCreatedByName(authorName);

        teamMapper.insert(row);

        // Creator becomes the first member.
        var m = new OwLfgTeamMember();
        m.setTeamId(row.getId());
        m.setAccountId(accountId);
        m.setDisplayName(authorName);
        m.setRoleTagsJson(null);
        m.setStatus("JOINED");
        m.setJoinedAt(now);
        m.setLeftAt(null);
        memberMapper.insert(m);

        auditLogService.success("OW_LFG_TEAM_CREATE", "OW_LFG_TEAM", String.valueOf(row.getId()), Map.of(
                "title", row.getTitle(),
                "modeCode", row.getModeCode()
        ));

        return new OwLfgTeamCreateResponse(row.getId(), row.getInviteCode());
    }

    public OwLfgMyStateDto myState(long teamId) {
        Long accountId = requireAccountId();
        OwLfgTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new IllegalArgumentException("Team not found");
        }
        LocalDateTime now = LocalDateTime.now();
        boolean leader = team.getCreatedByAccountId() != null && team.getCreatedByAccountId().equals(accountId);

        OwLfgTeamMember member = memberMapper.selectOne(new LambdaQueryWrapper<OwLfgTeamMember>()
                .eq(OwLfgTeamMember::getTeamId, teamId)
                .eq(OwLfgTeamMember::getAccountId, accountId)
                .last("LIMIT 1"));
        boolean isMember = member != null && "JOINED".equals(member.getStatus());

        OwLfgTeamJoinRequest jr = joinRequestMapper.selectOne(new LambdaQueryWrapper<OwLfgTeamJoinRequest>()
                .eq(OwLfgTeamJoinRequest::getTeamId, teamId)
                .eq(OwLfgTeamJoinRequest::getAccountId, accountId)
                .last("LIMIT 1"));
        Long jrId = jr != null ? jr.getId() : null;
        String jrStatus = jr != null ? jr.getStatus() : null;

        boolean expired = team.getExpiresAt() != null && team.getExpiresAt().isBefore(now);
        boolean open = "OPEN".equals(team.getStatus()) && !expired;
        boolean full = nvl(team.getMemberCount()) >= nvl(team.getCapacity());

        boolean canJoin = open && !full && Boolean.TRUE.equals(team.getAutoApprove()) && !isMember;
        boolean canRequest = open && !full && !Boolean.TRUE.equals(team.getAutoApprove()) && !isMember
                && (jrStatus == null || "REJECTED".equals(jrStatus) || "CANCELED".equals(jrStatus));

        return new OwLfgMyStateDto(leader, isMember, jrId, jrStatus, canJoin, canRequest);
    }

    @Transactional
    public void join(long teamId) {
        AppAccount acc = requireAccount();
        OwLfgTeam team = teamMapper.selectForUpdateById(teamId);
        if (team == null) throw new IllegalArgumentException("Team not found");

        ensureJoinable(team);
        if (!Boolean.TRUE.equals(team.getAutoApprove())) {
            throw new IllegalArgumentException("Team requires approval");
        }

        upsertJoinMember(team, acc, false);
        auditLogService.success("OW_LFG_JOIN", "OW_LFG_TEAM", String.valueOf(teamId), null);
    }

    @Transactional
    public OwLfgJoinRequestDto requestJoin(long teamId, OwLfgJoinRequestCreateRequest req) {
        AppAccount acc = requireAccount();
        OwLfgTeam team = teamMapper.selectForUpdateById(teamId);
        if (team == null) throw new IllegalArgumentException("Team not found");

        ensureJoinable(team);
        if (Boolean.TRUE.equals(team.getAutoApprove())) {
            throw new IllegalArgumentException("Team is open-join");
        }

        // Already a member?
        OwLfgTeamMember member = memberMapper.selectOne(new LambdaQueryWrapper<OwLfgTeamMember>()
                .eq(OwLfgTeamMember::getTeamId, teamId)
                .eq(OwLfgTeamMember::getAccountId, acc.getId())
                .last("LIMIT 1"));
        if (member != null && "JOINED".equals(member.getStatus())) {
            throw new IllegalArgumentException("Already joined");
        }

        OwLfgTeamJoinRequest existing = joinRequestMapper.selectOne(new LambdaQueryWrapper<OwLfgTeamJoinRequest>()
                .eq(OwLfgTeamJoinRequest::getTeamId, teamId)
                .eq(OwLfgTeamJoinRequest::getAccountId, acc.getId())
                .last("LIMIT 1"));
        String msg = trimToNull(req != null ? req.message() : null);

        if (existing == null) {
            var row = new OwLfgTeamJoinRequest();
            row.setTeamId(teamId);
            row.setAccountId(acc.getId());
            row.setDisplayName(displayNameSnapshot(acc));
            row.setMessage(msg);
            row.setStatus("PENDING");
            joinRequestMapper.insert(row);
            return toJoinRequestDto(row);
        }

        if ("PENDING".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Already requested");
        }
        if (!"REJECTED".equals(existing.getStatus()) && !"CANCELED".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Cannot request");
        }

        existing.setStatus("PENDING");
        existing.setMessage(msg);
        joinRequestMapper.updateById(existing);
        return toJoinRequestDto(existing);
    }

    @Transactional
    public void cancelJoinRequest(long joinRequestId) {
        Long accountId = requireAccountId();
        OwLfgTeamJoinRequest row = joinRequestMapper.selectForUpdateById(joinRequestId);
        if (row == null) throw new IllegalArgumentException("Join request not found");
        if (!accountId.equals(row.getAccountId())) {
            throw new IllegalArgumentException("Not allowed");
        }
        if (!"PENDING".equals(row.getStatus())) {
            throw new IllegalArgumentException("Join request not pending");
        }
        row.setStatus("CANCELED");
        joinRequestMapper.updateById(row);
    }

    @Transactional
    public void leave(long teamId) {
        Long accountId = requireAccountId();
        OwLfgTeam team = teamMapper.selectForUpdateById(teamId);
        if (team == null) throw new IllegalArgumentException("Team not found");
        if (team.getCreatedByAccountId() != null && team.getCreatedByAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Leader cannot leave; disband the team");
        }

        OwLfgTeamMember m = memberMapper.selectOne(new LambdaQueryWrapper<OwLfgTeamMember>()
                .eq(OwLfgTeamMember::getTeamId, teamId)
                .eq(OwLfgTeamMember::getAccountId, accountId)
                .last("LIMIT 1"));
        if (m == null || !"JOINED".equals(m.getStatus())) {
            throw new IllegalArgumentException("Not in team");
        }

        m.setStatus("LEFT");
        m.setLeftAt(LocalDateTime.now());
        memberMapper.updateById(m);

        team.setMemberCount(Math.max(0, nvl(team.getMemberCount()) - 1));
        teamMapper.updateById(team);
    }

    public OwLfgMyTeamsDto myTeams() {
        Long accountId = requireAccountId();
        LocalDateTime now = LocalDateTime.now();

        var createdRows = teamMapper.selectList(new LambdaQueryWrapper<OwLfgTeam>()
                .eq(OwLfgTeam::getCreatedByAccountId, accountId)
                .orderByDesc(OwLfgTeam::getCreatedAt)
                .orderByDesc(OwLfgTeam::getId)
                .last("LIMIT 50"));

        var memberRows = memberMapper.selectList(new LambdaQueryWrapper<OwLfgTeamMember>()
                .select(OwLfgTeamMember::getTeamId)
                .eq(OwLfgTeamMember::getAccountId, accountId)
                .eq(OwLfgTeamMember::getStatus, "JOINED"));
        Set<Long> teamIds = new HashSet<>();
        for (var m : memberRows) {
            if (m.getTeamId() != null) teamIds.add(m.getTeamId());
        }

        List<OwLfgTeam> joinedRows;
        if (teamIds.isEmpty()) {
            joinedRows = List.of();
        } else {
            joinedRows = teamMapper.selectList(new LambdaQueryWrapper<OwLfgTeam>()
                    .in(OwLfgTeam::getId, teamIds)
                    .orderByDesc(OwLfgTeam::getUpdatedAt)
                    .orderByDesc(OwLfgTeam::getId));
        }

        return new OwLfgMyTeamsDto(
                createdRows.stream().map(t -> toSummary(t, now)).toList(),
                joinedRows.stream().map(t -> toSummary(t, now)).toList()
        );
    }

    public OwLfgTeamDetailDto teamDetail(long teamId) {
        Long accountId = requireAccountId();
        OwLfgTeam team = teamMapper.selectById(teamId);
        if (team == null) throw new IllegalArgumentException("Team not found");

        boolean leader = team.getCreatedByAccountId() != null && team.getCreatedByAccountId().equals(accountId);
        boolean member = leader || isJoinedMember(teamId, accountId);
        if (!member) throw new IllegalArgumentException("Not allowed");

        LocalDateTime now = LocalDateTime.now();
        String status = normalizeStatus(team, now);

        List<OwLfgTeamMember> members = memberMapper.listJoined(teamId);
        List<OwLfgMemberDto> memberDtos = members.stream().map(m -> toMemberDto(m, team.getCreatedByAccountId())).toList();

        List<OwLfgJoinRequestDto> pending = List.of();
        if (leader && !Boolean.TRUE.equals(team.getAutoApprove())) {
            pending = joinRequestMapper.listByTeamAndStatus(teamId, "PENDING").stream().map(this::toJoinRequestDto).toList();
        }

        return new OwLfgTeamDetailDto(
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
                readContact(team.getContactJson()),
                team.getCreatedByName(),
                status,
                team.getExpiresAt(),
                team.getCreatedAt(),
                memberDtos,
                pending
        );
    }

    @Transactional
    public void approveJoinRequest(long joinRequestId) {
        Long leaderId = requireAccountId();
        OwLfgTeamJoinRequest jr = joinRequestMapper.selectForUpdateById(joinRequestId);
        if (jr == null) throw new IllegalArgumentException("Join request not found");
        if (!"PENDING".equals(jr.getStatus())) throw new IllegalArgumentException("Join request not pending");

        OwLfgTeam team = teamMapper.selectForUpdateById(jr.getTeamId());
        if (team == null) throw new IllegalArgumentException("Team not found");
        ensureLeader(team, leaderId);
        ensureJoinable(team);
        if (Boolean.TRUE.equals(team.getAutoApprove())) throw new IllegalArgumentException("Team is open-join");

        AppAccount acc = accountMapper.selectById(jr.getAccountId());
        if (acc == null || !Boolean.TRUE.equals(acc.getEnabled())) {
            jr.setStatus("REJECTED");
            joinRequestMapper.updateById(jr);
            throw new IllegalArgumentException("Account not found");
        }

        upsertJoinMember(team, acc, true);
        jr.setStatus("APPROVED");
        joinRequestMapper.updateById(jr);
    }

    @Transactional
    public void rejectJoinRequest(long joinRequestId) {
        Long leaderId = requireAccountId();
        OwLfgTeamJoinRequest jr = joinRequestMapper.selectForUpdateById(joinRequestId);
        if (jr == null) throw new IllegalArgumentException("Join request not found");
        OwLfgTeam team = teamMapper.selectForUpdateById(jr.getTeamId());
        if (team == null) throw new IllegalArgumentException("Team not found");
        ensureLeader(team, leaderId);
        if (!"PENDING".equals(jr.getStatus())) throw new IllegalArgumentException("Join request not pending");
        jr.setStatus("REJECTED");
        joinRequestMapper.updateById(jr);
    }

    @Transactional
    public void kick(long teamId, long memberId) {
        Long leaderId = requireAccountId();
        OwLfgTeam team = teamMapper.selectForUpdateById(teamId);
        if (team == null) throw new IllegalArgumentException("Team not found");
        ensureLeader(team, leaderId);

        OwLfgTeamMember m = memberMapper.selectById(memberId);
        if (m == null || !Objects.equals(m.getTeamId(), teamId)) {
            throw new IllegalArgumentException("Member not found");
        }
        if (!"JOINED".equals(m.getStatus())) {
            throw new IllegalArgumentException("Member not active");
        }
        if (Objects.equals(m.getAccountId(), leaderId)) {
            throw new IllegalArgumentException("Cannot kick leader");
        }

        m.setStatus("KICKED");
        m.setLeftAt(LocalDateTime.now());
        memberMapper.updateById(m);

        team.setMemberCount(Math.max(0, nvl(team.getMemberCount()) - 1));
        teamMapper.updateById(team);
    }

    @Transactional
    public void close(long teamId) {
        Long leaderId = requireAccountId();
        OwLfgTeam team = teamMapper.selectForUpdateById(teamId);
        if (team == null) throw new IllegalArgumentException("Team not found");
        ensureLeader(team, leaderId);
        if (!"OPEN".equals(team.getStatus())) {
            throw new IllegalArgumentException("Team not open");
        }
        team.setStatus("CLOSED");
        teamMapper.updateById(team);
    }

    @Transactional
    public void disband(long teamId) {
        Long leaderId = requireAccountId();
        OwLfgTeam team = teamMapper.selectForUpdateById(teamId);
        if (team == null) throw new IllegalArgumentException("Team not found");
        ensureLeader(team, leaderId);
        if ("DISBANDED".equals(team.getStatus())) return;
        team.setStatus("DISBANDED");
        teamMapper.updateById(team);
    }

    @Transactional
    public void report(OwLfgReportCreateRequest req) {
        Long accountId = requireAccountId();
        String targetType = req.targetType().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("TEAM", "ACCOUNT").contains(targetType)) {
            throw new IllegalArgumentException("Invalid targetType");
        }

        var row = new OwLfgReport();
        row.setReporterAccountId(accountId);
        row.setTargetType(targetType);
        row.setTargetId(req.targetId());
        row.setReason(req.reason().trim());
        row.setDetail(trimToNull(req.detail()));

        if ("TEAM".equals(targetType)) {
            OwLfgTeam team = teamMapper.selectById(req.targetId());
            if (team != null) {
                row.setSnapshotJson(toJson(Map.of(
                        "title", team.getTitle(),
                        "modeCode", team.getModeCode(),
                        "inviteCode", team.getInviteCode()
                )));
            }
        }

        reportMapper.insert(row);
    }

    private void enforceCreateLimits(Long accountId) {
        // 1) At most 1 OPEN team per 10 minutes.
        LocalDateTime tenMinAgo = LocalDateTime.now().minusMinutes(10);
        Long recentOpen = teamMapper.selectCount(new LambdaQueryWrapper<OwLfgTeam>()
                .eq(OwLfgTeam::getCreatedByAccountId, accountId)
                .eq(OwLfgTeam::getStatus, "OPEN")
                .gt(OwLfgTeam::getCreatedAt, tenMinAgo));
        if (recentOpen != null && recentOpen > 0) {
            throw new IllegalArgumentException("Too frequent; please try later");
        }

        // 2) At most 3 OPEN teams at the same time.
        Long openCount = teamMapper.selectCount(new LambdaQueryWrapper<OwLfgTeam>()
                .eq(OwLfgTeam::getCreatedByAccountId, accountId)
                .eq(OwLfgTeam::getStatus, "OPEN")
                .gt(OwLfgTeam::getExpiresAt, LocalDateTime.now()));
        if (openCount != null && openCount >= 3) {
            throw new IllegalArgumentException("Too many open teams");
        }
    }

    private void validateCreateRequest(OwLfgTeamCreateRequest req) {
        String mode = req.modeCode().trim().toUpperCase(Locale.ROOT);
        if (!MODE_CODES.contains(mode)) {
            throw new IllegalArgumentException("Invalid modeCode");
        }
        String platform = req.platformCode().trim().toUpperCase(Locale.ROOT);
        if (!PLATFORM_CODES.contains(platform)) {
            throw new IllegalArgumentException("Invalid platformCode");
        }
        if (req.rankMin() != null && !req.rankMin().isBlank() && !RANK_ORDER.contains(req.rankMin().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid rankMin");
        }
        if (req.rankMax() != null && !req.rankMax().isBlank() && !RANK_ORDER.contains(req.rankMax().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid rankMax");
        }
        if (req.rankMin() != null && !req.rankMin().isBlank() && req.rankMax() != null && !req.rankMax().isBlank()) {
            int a = RANK_ORDER.indexOf(req.rankMin().trim().toUpperCase(Locale.ROOT));
            int b = RANK_ORDER.indexOf(req.rankMax().trim().toUpperCase(Locale.ROOT));
            if (a >= 0 && b >= 0 && a > b) {
                throw new IllegalArgumentException("rankMin must be <= rankMax");
            }
        }
    }

    private void ensureJoinable(OwLfgTeam team) {
        LocalDateTime now = LocalDateTime.now();
        if (!"OPEN".equals(team.getStatus())) {
            throw new IllegalArgumentException("Team not open");
        }
        if (team.getExpiresAt() != null && team.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException("Team expired");
        }
        if (nvl(team.getMemberCount()) >= nvl(team.getCapacity())) {
            throw new IllegalArgumentException("Team is full");
        }
    }

    private void ensureLeader(OwLfgTeam team, Long accountId) {
        if (team.getCreatedByAccountId() == null || !team.getCreatedByAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Not leader");
        }
    }

    private void upsertJoinMember(OwLfgTeam team, AppAccount acc, boolean fromApproval) {
        Long teamId = team.getId();
        Long accountId = acc.getId();
        String dn = displayNameSnapshot(acc);

        OwLfgTeamMember existing = memberMapper.selectOne(new LambdaQueryWrapper<OwLfgTeamMember>()
                .eq(OwLfgTeamMember::getTeamId, teamId)
                .eq(OwLfgTeamMember::getAccountId, accountId)
                .last("LIMIT 1"));

        if (existing == null) {
            var m = new OwLfgTeamMember();
            m.setTeamId(teamId);
            m.setAccountId(accountId);
            m.setDisplayName(dn);
            m.setRoleTagsJson(null);
            m.setStatus("JOINED");
            m.setJoinedAt(LocalDateTime.now());
            m.setLeftAt(null);
            memberMapper.insert(m);
            team.setMemberCount(nvl(team.getMemberCount()) + 1);
            teamMapper.updateById(team);
            return;
        }

        if ("JOINED".equals(existing.getStatus())) {
            // idempotent join
            return;
        }
        if ("KICKED".equals(existing.getStatus()) && !fromApproval) {
            throw new IllegalArgumentException("You were kicked from this team");
        }

        existing.setStatus("JOINED");
        existing.setDisplayName(dn);
        existing.setJoinedAt(LocalDateTime.now());
        existing.setLeftAt(null);
        memberMapper.updateById(existing);

        team.setMemberCount(nvl(team.getMemberCount()) + 1);
        teamMapper.updateById(team);
    }

    private boolean isJoinedMember(long teamId, long accountId) {
        Long cnt = memberMapper.selectCount(new LambdaQueryWrapper<OwLfgTeamMember>()
                .eq(OwLfgTeamMember::getTeamId, teamId)
                .eq(OwLfgTeamMember::getAccountId, accountId)
                .eq(OwLfgTeamMember::getStatus, "JOINED"));
        return cnt != null && cnt > 0;
    }

    private OwLfgTeamSummaryDto toSummary(OwLfgTeam t, LocalDateTime now) {
        return new OwLfgTeamSummaryDto(
                t.getId(),
                t.getInviteCode(),
                t.getTitle(),
                t.getModeCode(),
                t.getPlatformCode(),
                Boolean.TRUE.equals(t.getAllowCrossplay()),
                nvl(t.getCapacity()),
                nvl(t.getMemberCount()),
                Boolean.TRUE.equals(t.getAutoApprove()),
                nvl(t.getVoiceRequired()),
                t.getRegionCode(),
                t.getLanguageCode(),
                t.getRankMin(),
                t.getRankMax(),
                readStringList(t.getNeedRolesJson()),
                readStringList(t.getPreferredHeroCodesJson()),
                readStringList(t.getTagsJson()),
                t.getCreatedByName(),
                normalizeStatus(t, now),
                t.getExpiresAt(),
                t.getCreatedAt()
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

    private OwLfgJoinRequestDto toJoinRequestDto(OwLfgTeamJoinRequest r) {
        return new OwLfgJoinRequestDto(
                r.getId(),
                r.getTeamId(),
                r.getAccountId(),
                r.getDisplayName(),
                r.getMessage(),
                r.getStatus(),
                r.getCreatedAt()
        );
    }

    private OwLfgContactDto normalizeContact(OwLfgContactDto c) {
        if (c == null) return null;
        String battleTag = trimToNull(c.battleTag());
        String voiceRoom = trimToNull(c.voiceRoom());
        String groupNo = trimToNull(c.groupNo());
        if (battleTag == null && voiceRoom == null && groupNo == null) return null;
        return new OwLfgContactDto(battleTag, voiceRoom, groupNo);
    }

    private OwLfgContactDto readContact(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, OwLfgContactDto.class);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> normalizeEnumList(List<String> in, Set<String> allowed, int max) {
        if (in == null || in.isEmpty()) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : in) {
            if (s == null) continue;
            String t = s.trim().toUpperCase(Locale.ROOT);
            if (t.isEmpty()) continue;
            if (!allowed.contains(t)) continue;
            set.add(t);
            if (set.size() >= max) break;
        }
        return set.stream().toList();
    }

    private List<String> normalizeList(List<String> in, int max) {
        if (in == null || in.isEmpty()) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : in) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isEmpty()) continue;
            set.add(t);
            if (set.size() >= max) break;
        }
        return set.stream().toList();
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

    private String toJson(Object o) {
        if (o == null) return null;
        if (o instanceof List<?> list && list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private AppAccount requireAccount() {
        Long accountId = requireAccountId();
        AppAccount acc = accountMapper.selectById(accountId);
        if (acc == null || !Boolean.TRUE.equals(acc.getEnabled())) {
            throw new IllegalArgumentException("Account not found");
        }
        return acc;
    }

    private Long requireAccountId() {
        Long accountId = LoginIdUtils.parseAccountId(StpUtil.getLoginId());
        if (accountId == null) {
            throw new IllegalArgumentException("Account login required");
        }
        return accountId;
    }

    private static String displayNameSnapshot(AppAccount acc) {
        if (acc == null) return "-";
        if (acc.getDisplayName() != null && !acc.getDisplayName().isBlank()) return acc.getDisplayName().trim();
        return acc.getUsername();
    }

    private String generateInviteCode() {
        // Upper-case base32 without ambiguous chars.
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            String code = sb.toString();
            Long exists = teamMapper.selectCount(new LambdaQueryWrapper<OwLfgTeam>().eq(OwLfgTeam::getInviteCode, code));
            if (exists == null || exists == 0) return code;
        }
        throw new IllegalStateException("Failed to generate invite code");
    }

    private static String normalizeStatus(OwLfgTeam team, LocalDateTime now) {
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

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String upper(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase(Locale.ROOT);
    }

    private static int nvl(Integer n) {
        return n != null ? n : 0;
    }
}
