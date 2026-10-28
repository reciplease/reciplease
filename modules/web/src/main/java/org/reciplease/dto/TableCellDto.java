package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.reciplease.model.TableCell;

@Schema(name = "TableCell")
public record TableCellDto(
        @NotBlank @Schema(requiredMode = REQUIRED) String text,
        @NotNull @Schema(requiredMode = REQUIRED) List<@Valid @NotNull TableCellDto> inputs,
        @Valid @JsonInclude(JsonInclude.Include.NON_NULL) CellDurationDto duration,
        @Schema(requiredMode = REQUIRED) Boolean handsOff) {

    public TableCellDto {
        handsOff = Boolean.TRUE.equals(handsOff);
    }

    public static TableCellDto from(final TableCell cell) {
        return new TableCellDto(
                cell.text(),
                cell.inputs().stream().map(TableCellDto::from).toList(),
                CellDurationDto.from(cell.duration()),
                cell.handsOff());
    }

    public TableCell toModel() {
        return new TableCell(
                text,
                inputs.stream().map(TableCellDto::toModel).toList(),
                duration == null ? null : duration.toModel(),
                handsOff);
    }
}
