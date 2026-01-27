package com.rcszh.gm.ow.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OwItemBindHeroesRequest(
        @NotNull List<Long> heroIds
) {
}

