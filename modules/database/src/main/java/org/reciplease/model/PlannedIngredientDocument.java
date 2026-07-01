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
    private List<InventoryAllocationDocument> allocations = new ArrayList<>();

    public static PlannedIngredientDocument from(final PlannedIngredient plannedIngredient) {
        return PlannedIngredientDocument.builder()
                .ingredient(RecipeIngredientDocument.from(plannedIngredient.ingredient()))
                .allocations(plannedIngredient.allocations().stream()
                        .map(InventoryAllocationDocument::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public PlannedIngredient toModel() {
        var resolvedAllocations = allocations == null ? List.<InventoryAllocation>of() : allocations.stream()
                .map(InventoryAllocationDocument::toModel)
                .collect(Collectors.toList());
        return new PlannedIngredient(ingredient.toModel(), resolvedAllocations);
    }
}
