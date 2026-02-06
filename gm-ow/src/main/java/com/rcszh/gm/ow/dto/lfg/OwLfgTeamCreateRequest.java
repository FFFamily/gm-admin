package com.rcszh.gm.ow.dto.lfg;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OwLfgTeamCreateRequest(
        @NotBlank @Size(min = 2, max = 128) String title,
        @NotBlank @Size(max = 32) String modeCode,
        @NotBlank @Size(max = 16) String platformCode,
        Boolean allowCrossplay,

        @NotNull @Min(2) @Max(10) Integer capacity,
        @NotNull Boolean autoApprove,
        @NotNull @Min(0) @Max(2) Integer voiceRequired,

        @Size(max = 32) String regionCode,
        @Size(max = 32) String languageCode,
        @Size(max = 16) String rankMin,
        @Size(max = 16) String rankMax,

        @Size(max = 3) List<@Size(max = 16) String> needRoles,
        @Size(max = 3) List<@Size(max = 64) String> preferredHeroCodes,
        @Size(max = 10) List<@Size(max = 32) String> tags,

        @Size(max = 512) String note,
        @Valid OwLfgContactDto contact,

        @NotNull @Min(30) @Max(10080) Integer durationMinutes
) {
}

