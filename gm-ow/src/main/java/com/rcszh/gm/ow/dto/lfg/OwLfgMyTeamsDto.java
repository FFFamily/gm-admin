package com.rcszh.gm.ow.dto.lfg;

import java.util.List;

public record OwLfgMyTeamsDto(
        List<OwLfgTeamSummaryDto> created,
        List<OwLfgTeamSummaryDto> joined
) {
}

