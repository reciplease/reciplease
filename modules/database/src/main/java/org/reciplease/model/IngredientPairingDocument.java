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
                .recipeIngredient(RecipeIngredientDocument.from(pairing.getRecipeIngredient()))
                .allocations(pairing.getAllocations().stream()
                        .map(InventoryAllocationDocument::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public IngredientPairing toModel() {
        return IngredientPairing.builder()
                .recipeIngredient(recipeIngredient.toModel())
                .allocations(allocations == null ? List.of() : allocations.stream()
                        .map(InventoryAllocationDocument::toModel)
                        .collect(Collectors.toList()))
                .build();
    }
}
