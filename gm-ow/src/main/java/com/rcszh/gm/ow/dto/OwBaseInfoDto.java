package com.rcszh.gm.ow.dto;

import java.util.List;

public record OwBaseInfoDto(
        Integer initialGold,
        List<OwStatDefDto> statDefs
) {
}

