package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.reciplease.model.RecipeMethod;
import org.reciplease.model.TableMethod;

@Schema(name = "TableMethod")
public record TableMethodDto(
        @NotNull @Schema(requiredMode = REQUIRED) List<@Valid @NotNull TableCellDto> cells) implements RecipeMethodDto {

    @JsonAnySetter
    void rejectUnknownProperty(final String name, final Object value) {
        throw new IllegalArgumentException("unknown table method property: " + name);
    }

    @Override
    public RecipeMethod toModel() {
        return new TableMethod(cells.stream().map(TableCellDto::toModel).toList());
    }
}
