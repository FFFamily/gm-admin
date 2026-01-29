package com.rcszh.gm.api.ow;

import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.ow.dto.community.OwLoadoutCreateRequest;
import com.rcszh.gm.ow.dto.community.OwLoadoutCreateResponse;
import com.rcszh.gm.ow.dto.community.OwLoadoutDetailDto;
import com.rcszh.gm.ow.dto.community.OwLoadoutInteractStateDto;
import com.rcszh.gm.ow.dto.community.OwLoadoutSummaryDto;
import com.rcszh.gm.ow.service.community.OwCommunityInteractionService;
import com.rcszh.gm.ow.service.community.OwCommunityQueryService;
import com.rcszh.gm.ow.service.community.OwCommunityWriteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ow/community/loadouts")
public class OwCommunityController {

    private final OwCommunityQueryService queryService;
    private final OwCommunityWriteService writeService;
    private final OwCommunityInteractionService interactionService;

    public OwCommunityController(OwCommunityQueryService queryService,
                                 OwCommunityWriteService writeService,
                                 OwCommunityInteractionService interactionService) {
        this.queryService = queryService;
        this.writeService = writeService;
        this.interactionService = interactionService;
    }

    @GetMapping
    public ApiResponse<PageResult<OwLoadoutSummaryDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String heroCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) Boolean featured
    ) {
        return ApiResponse.ok(queryService.list(page, size, heroCode, keyword, sort, featured));
    }

    @GetMapping("/{id}")
    public ApiResponse<OwLoadoutDetailDto> detail(@PathVariable("id") long id) {
        return ApiResponse.ok(queryService.detail(id));
    }

    @PostMapping
    public ApiResponse<OwLoadoutCreateResponse> create(@Valid @RequestBody OwLoadoutCreateRequest req) {
        return ApiResponse.ok(writeService.create(req));
    }

    // Account required: like/favorite
    @PutMapping("/{id}/like")
    public ApiResponse<OwLoadoutInteractStateDto> like(@PathVariable("id") long id) {
        return ApiResponse.ok(interactionService.like(id));
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<OwLoadoutInteractStateDto> unlike(@PathVariable("id") long id) {
        return ApiResponse.ok(interactionService.unlike(id));
    }

    @PutMapping("/{id}/favorite")
    public ApiResponse<OwLoadoutInteractStateDto> favorite(@PathVariable("id") long id) {
        return ApiResponse.ok(interactionService.favorite(id));
    }

    @DeleteMapping("/{id}/favorite")
    public ApiResponse<OwLoadoutInteractStateDto> unfavorite(@PathVariable("id") long id) {
        return ApiResponse.ok(interactionService.unfavorite(id));
    }

    @GetMapping("/{id}/my-state")
    public ApiResponse<OwLoadoutInteractStateDto> myState(@PathVariable("id") long id) {
        return ApiResponse.ok(interactionService.myState(id));
    }
}
