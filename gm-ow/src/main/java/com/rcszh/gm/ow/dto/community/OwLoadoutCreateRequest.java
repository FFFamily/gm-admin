package com.rcszh.gm.ow.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OwLoadoutCreateRequest(
        @NotBlank String heroCode,
        @NotBlank @Size(min = 2, max = 128) String title,
        @Size(max = 512) String description,
        @NotNull @Size(min = 1, max = 6) List<String> itemCodes
) {
}

