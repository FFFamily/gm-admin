package com.rcszh.gm.api.ow;

import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.ow.dto.community.OwCommunityHeroStatDto;
import com.rcszh.gm.ow.service.community.OwCommunityQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ow/community")
public class OwCommunityMetaController {

    private final OwCommunityQueryService queryService;

    public OwCommunityMetaController(OwCommunityQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/heroes")
    public ApiResponse<List<OwCommunityHeroStatDto>> heroes() {
        return ApiResponse.ok(queryService.heroStats());
    }
}

