package com.rcszh.gm.api.ow;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.admin.*;
import com.rcszh.gm.ow.service.admin.OwItemAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ow/items")
public class OwAdminItemController {

    private final OwItemAdminService itemAdminService;

    public OwAdminItemController(OwItemAdminService itemAdminService) {
        this.itemAdminService = itemAdminService;
    }

    @SaCheckPermission("ow:item:list")
    @GetMapping
    public ApiResponse<PageResult<OwAdminItemDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String quality,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean isGlobal
    ) {
        return ApiResponse.ok(itemAdminService.list(page, size, keyword, category, quality, enabled, isGlobal));
    }

    @SaCheckPermission("ow:item:read")
    @GetMapping("/{id}")
    public ApiResponse<OwAdminItemDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(itemAdminService.detail(id));
    }

    @SaCheckPermission("ow:item:create")
    @PostMapping
    public ApiResponse<OwAdminItemDetailDto> create(@Valid @RequestBody OwItemCreateRequest req) {
        return ApiResponse.ok(itemAdminService.create(req));
    }

    @SaCheckPermission("ow:item:update")
    @PutMapping("/{id}")
    public ApiResponse<OwAdminItemDetailDto> update(@PathVariable("id") long id, @Valid @RequestBody OwItemUpdateRequest req) {
        return ApiResponse.ok(itemAdminService.update(id, req));
    }

    @SaCheckPermission("ow:item:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") long id) {
        itemAdminService.delete(id);
        return ApiResponse.ok(null);
    }

    @SaCheckPermission("ow:item:bind_heroes")
    @PostMapping("/{id}/heroes")
    public ApiResponse<OwAdminItemDetailDto> bindHeroes(@PathVariable("id") long id, @Valid @RequestBody OwItemBindHeroesRequest req) {
        return ApiResponse.ok(itemAdminService.bindHeroes(id, req));
    }
}
