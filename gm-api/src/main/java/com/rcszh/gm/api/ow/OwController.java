package com.rcszh.gm.api.ow;

import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.ow.dto.*;
import com.rcszh.gm.ow.service.OwPublicQueryService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ow")
public class OwController {

    private final OwPublicQueryService owPublicQueryService;

    public OwController(OwPublicQueryService owPublicQueryService) {
        this.owPublicQueryService = owPublicQueryService;
    }

    @GetMapping("/base-info")
    public ApiResponse<OwBaseInfoDto> baseInfo() {
        return ApiResponse.ok(owPublicQueryService.baseInfo());
    }

    @GetMapping("/heroes")
    public ApiResponse<List<OwHeroSummaryDto>> heroes() {
        return ApiResponse.ok(owPublicQueryService.heroes());
    }

    @GetMapping("/heroes/{heroCode}")
    public ApiResponse<OwHeroDetailDto> heroDetail(@PathVariable("heroCode") @NotBlank String heroCode) {
        return ApiResponse.ok(owPublicQueryService.heroDetail(heroCode));
    }

    @GetMapping("/items")
    public ApiResponse<List<OwItemDto>> items(
            @RequestParam @NotBlank String heroCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String quality
    ) {
        return ApiResponse.ok(owPublicQueryService.items(heroCode, category, quality));
    }
}
