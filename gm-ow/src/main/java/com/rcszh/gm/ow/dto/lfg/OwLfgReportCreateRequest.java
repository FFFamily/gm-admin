package com.rcszh.gm.ow.dto.lfg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OwLfgReportCreateRequest(
        @NotBlank @Size(max = 16) String targetType,
        @NotNull Long targetId,
        @NotBlank @Size(max = 64) String reason,
        @Size(max = 512) String detail
) {
}

