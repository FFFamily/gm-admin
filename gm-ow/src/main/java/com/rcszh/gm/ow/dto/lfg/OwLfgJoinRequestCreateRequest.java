package com.rcszh.gm.ow.dto.lfg;

import jakarta.validation.constraints.Size;

public record OwLfgJoinRequestCreateRequest(
        @Size(max = 140) String message
) {
}

