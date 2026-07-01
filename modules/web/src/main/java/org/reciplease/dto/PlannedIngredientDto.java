package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PlannedIngredient;

import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "PlannedIngredient")
public class PlannedIngredientDto {

    RecipeIngredientDto ingredient;
    List<InventoryAllocationDto> allocations;

    public static PlannedIngredientDto from(final PlannedIngredient plannedIngredient) {
        return PlannedIngredientDto.builder()
                .ingredient(RecipeIngredientDto.from(plannedIngredient.ingredient()))
                .allocations(plannedIngredient.allocations().stream()
                        .map(InventoryAllocationDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public PlannedIngredient toModel() {
        return new PlannedIngredient(ingredient.toModel(),
                allocations == null ? List.of() : allocations.stream()
                        .map(InventoryAllocationDto::toModel)
                        .collect(Collectors.toList()));
    }
}
