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
public class IngredientPairingDocument {

    private RecipeIngredientDocument recipeIngredient;
    @Builder.Default
    private List<InventoryAllocationDocument> allocations = new ArrayList<>();

    public static IngredientPairingDocument from(final IngredientPairing pairing) {
        return IngredientPairingDocument.builder()
                .recipeIngredient(RecipeIngredientDocument.from(pairing.recipeIngredient()))
                .allocations(pairing.allocations().stream()
                        .map(InventoryAllocationDocument::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public IngredientPairing toModel() {
        var resolvedAllocations = allocations == null ? List.<InventoryAllocation>of() : allocations.stream()
                .map(InventoryAllocationDocument::toModel)
                .collect(Collectors.toList());
        return new IngredientPairing(recipeIngredient.toModel(), resolvedAllocations);
    }
}
