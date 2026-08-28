package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlannedIngredientDocument {

    private RecipeIngredientDocument ingredient;
    @Builder.Default
    private List<PantryAllocationDocument> allocations = new ArrayList<>();

    public static PlannedIngredientDocument from(final PlannedIngredient plannedIngredient) {
        return PlannedIngredientDocument.builder()
                .ingredient(RecipeIngredientDocument.from(plannedIngredient.ingredient()))
                .allocations(plannedIngredient.allocations().stream()
                        .map(PantryAllocationDocument::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public PlannedIngredient toModel() {
        var resolvedAllocations = allocations == null ? List.<PantryAllocation>of() : allocations.stream()
                .map(PantryAllocationDocument::toModel)
                .collect(Collectors.toList());
        return new PlannedIngredient(ingredient.toModel(), resolvedAllocations);
    }
}
