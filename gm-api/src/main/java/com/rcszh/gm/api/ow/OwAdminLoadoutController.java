package com.rcszh.gm.api.ow;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.admin.*;
import com.rcszh.gm.ow.service.admin.OwLoadoutAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ow/loadouts")
public class OwAdminLoadoutController {

    private final OwLoadoutAdminService loadoutAdminService;

    public OwAdminLoadoutController(OwLoadoutAdminService loadoutAdminService) {
        this.loadoutAdminService = loadoutAdminService;
    }

    @SaCheckPermission("ow:loadout:list")
    @GetMapping
    public ApiResponse<PageResult<OwAdminLoadoutDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String heroCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean pinned
    ) {
        return ApiResponse.ok(loadoutAdminService.list(page, size, keyword, heroCode, status, featured, pinned));
    }

    @SaCheckPermission("ow:loadout:read")
    @GetMapping("/{id}")
    public ApiResponse<OwAdminLoadoutDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(loadoutAdminService.detail(id));
    }

    @SaCheckPermission("ow:loadout:update_status")
    @PutMapping("/{id}/status")
    public ApiResponse<OwAdminLoadoutDetailDto> setStatus(@PathVariable("id") long id, @Valid @RequestBody OwLoadoutSetStatusRequest req) {
        return ApiResponse.ok(loadoutAdminService.setStatus(id, req.status()));
    }

    @SaCheckPermission("ow:loadout:feature")
    @PutMapping("/{id}/featured")
    public ApiResponse<OwAdminLoadoutDetailDto> setFeatured(@PathVariable("id") long id, @Valid @RequestBody OwLoadoutSetFeaturedRequest req) {
        return ApiResponse.ok(loadoutAdminService.setFeatured(id, req.featured()));
    }

    @SaCheckPermission("ow:loadout:pin")
    @PutMapping("/{id}/pinned")
    public ApiResponse<OwAdminLoadoutDetailDto> setPinned(@PathVariable("id") long id, @Valid @RequestBody OwLoadoutSetPinnedRequest req) {
        return ApiResponse.ok(loadoutAdminService.setPinned(id, req.pinned()));
    }
}

