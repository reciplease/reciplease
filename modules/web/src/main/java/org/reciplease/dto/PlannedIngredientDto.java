package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PlannedIngredient;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "PlannedIngredient")
public class PlannedIngredientDto {

    @Schema(requiredMode = REQUIRED)
    RecipeIngredientDto ingredient;

    @Schema(requiredMode = REQUIRED)
    List<PantryAllocationDto> allocations;

    public static PlannedIngredientDto from(final PlannedIngredient plannedIngredient) {
        return PlannedIngredientDto.builder()
                .ingredient(RecipeIngredientDto.from(plannedIngredient.ingredient()))
                .allocations(plannedIngredient.allocations().stream()
                        .map(PantryAllocationDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public PlannedIngredient toModel() {
        return new PlannedIngredient(
                ingredient.toModel(),
                allocations == null
                        ? List.of()
                        : allocations.stream().map(PantryAllocationDto::toModel).collect(Collectors.toList()));
    }
}
