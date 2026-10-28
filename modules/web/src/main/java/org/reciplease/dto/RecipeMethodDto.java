package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.reciplease.model.ListMethod;
import org.reciplease.model.RecipeMethod;
import org.reciplease.model.TableMethod;

@Schema(
        name = "RecipeMethod",
        oneOf = {ListMethodDto.class, TableMethodDto.class})
public sealed interface RecipeMethodDto permits ListMethodDto, TableMethodDto {

    static RecipeMethodDto from(final RecipeMethod method) {
        return switch (method) {
            case ListMethod list -> new ListMethodDto(list.steps());
            case TableMethod table ->
                new TableMethodDto(
                        table.cells().stream().map(TableCellDto::from).toList());
        };
    }

    RecipeMethod toModel();
}
