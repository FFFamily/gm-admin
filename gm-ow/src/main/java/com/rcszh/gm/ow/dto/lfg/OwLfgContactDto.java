package com.rcszh.gm.ow.dto.lfg;

import jakarta.validation.constraints.Size;

public record OwLfgContactDto(
        @Size(max = 64) String battleTag,
        @Size(max = 128) String voiceRoom,
        @Size(max = 128) String groupNo
) {
}

