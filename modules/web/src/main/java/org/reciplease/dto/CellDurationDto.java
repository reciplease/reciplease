package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.reciplease.model.CellDuration;

@Schema(name = "CellDuration")
public record CellDurationDto(
        @NotNull @Min(0) @Schema(requiredMode = REQUIRED) Integer minMinutes,
        @NotNull @Schema(requiredMode = REQUIRED) Integer maxMinutes) {

    public static CellDurationDto from(final CellDuration duration) {
        return duration == null ? null : new CellDurationDto(duration.minMinutes(), duration.maxMinutes());
    }

    public CellDuration toModel() {
        return new CellDuration(minMinutes, maxMinutes);
    }

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "maxMinutes must not be below minMinutes")
    boolean isRangeOrdered() {
        return minMinutes == null || maxMinutes == null || maxMinutes >= minMinutes;
    }
}
