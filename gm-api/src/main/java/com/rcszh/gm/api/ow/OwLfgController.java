package com.rcszh.gm.api.ow;

import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.lfg.*;
import com.rcszh.gm.ow.service.lfg.OwLfgAccountService;
import com.rcszh.gm.ow.service.lfg.OwLfgQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ow/lfg")
public class OwLfgController {

    private final OwLfgQueryService queryService;
    private final OwLfgAccountService accountService;

    public OwLfgController(OwLfgQueryService queryService, OwLfgAccountService accountService) {
        this.queryService = queryService;
        this.accountService = accountService;
    }

    // Public
    @GetMapping("/teams")
    public ApiResponse<PageResult<OwLfgTeamSummaryDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String modeCode,
            @RequestParam(required = false) String platformCode,
            @RequestParam(required = false) Integer voiceRequired,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String needRole
    ) {
        return ApiResponse.ok(queryService.list(page, size, keyword, modeCode, platformCode, voiceRequired, status, needRole));
    }

    @GetMapping("/t/{inviteCode}")
    public ApiResponse<OwLfgTeamInviteDto> invite(@PathVariable("inviteCode") String inviteCode) {
        return ApiResponse.ok(queryService.invite(inviteCode));
    }

    // Account required
    @PostMapping("/teams")
    public ApiResponse<OwLfgTeamCreateResponse> create(@Valid @RequestBody OwLfgTeamCreateRequest req) {
        return ApiResponse.ok(accountService.create(req));
    }

    @GetMapping("/teams/{id}")
    public ApiResponse<OwLfgTeamDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(accountService.teamDetail(id));
    }

    @GetMapping("/teams/{id}/my-state")
    public ApiResponse<OwLfgMyStateDto> myState(@PathVariable("id") long id) {
        return ApiResponse.ok(accountService.myState(id));
    }

    @PostMapping("/teams/{id}/join")
    public ApiResponse<Void> join(@PathVariable("id") long id) {
        accountService.join(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/teams/{id}/join-requests")
    public ApiResponse<OwLfgJoinRequestDto> requestJoin(@PathVariable("id") long id,
                                                        @Valid @RequestBody(required = false) OwLfgJoinRequestCreateRequest req) {
        return ApiResponse.ok(accountService.requestJoin(id, req));
    }

    @PostMapping("/join-requests/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable("id") long id) {
        accountService.cancelJoinRequest(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/join-requests/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable("id") long id) {
        accountService.approveJoinRequest(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/join-requests/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable("id") long id) {
        accountService.rejectJoinRequest(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/teams/{teamId}/members/{memberId}/kick")
    public ApiResponse<Void> kick(@PathVariable("teamId") long teamId, @PathVariable("memberId") long memberId) {
        accountService.kick(teamId, memberId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/teams/{id}/leave")
    public ApiResponse<Void> leave(@PathVariable("id") long id) {
        accountService.leave(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/teams/{id}/close")
    public ApiResponse<Void> close(@PathVariable("id") long id) {
        accountService.close(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/teams/{id}/disband")
    public ApiResponse<Void> disband(@PathVariable("id") long id) {
        accountService.disband(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/my/teams")
    public ApiResponse<OwLfgMyTeamsDto> myTeams() {
        return ApiResponse.ok(accountService.myTeams());
    }

    @PostMapping("/reports")
    public ApiResponse<Void> report(@Valid @RequestBody OwLfgReportCreateRequest req) {
        accountService.report(req);
        return ApiResponse.ok(null);
    }
}

