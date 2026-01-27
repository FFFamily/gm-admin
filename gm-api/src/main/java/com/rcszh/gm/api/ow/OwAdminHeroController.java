package com.rcszh.gm.api.ow;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.admin.*;
import com.rcszh.gm.ow.service.admin.OwHeroAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ow/heroes")
public class OwAdminHeroController {

    private final OwHeroAdminService heroAdminService;

    public OwAdminHeroController(OwHeroAdminService heroAdminService) {
        this.heroAdminService = heroAdminService;
    }

    @SaCheckPermission("ow:hero:list")
    @GetMapping
    public ApiResponse<PageResult<OwAdminHeroDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.ok(heroAdminService.list(page, size, keyword, enabled));
    }

    @SaCheckPermission("ow:hero:read")
    @GetMapping("/{id}")
    public ApiResponse<OwAdminHeroDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(heroAdminService.detail(id));
    }

    @SaCheckPermission("ow:hero:create")
    @PostMapping
    public ApiResponse<OwAdminHeroDetailDto> create(@Valid @RequestBody OwHeroCreateRequest req) {
        return ApiResponse.ok(heroAdminService.create(req));
    }

    @SaCheckPermission("ow:hero:update")
    @PutMapping("/{id}")
    public ApiResponse<OwAdminHeroDetailDto> update(@PathVariable("id") long id, @Valid @RequestBody OwHeroUpdateRequest req) {
        return ApiResponse.ok(heroAdminService.update(id, req));
    }

    @SaCheckPermission("ow:hero:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") long id) {
        heroAdminService.delete(id);
        return ApiResponse.ok(null);
    }
}
