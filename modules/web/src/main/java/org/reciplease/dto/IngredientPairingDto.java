package org.reciplease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.IngredientPairing;

import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
@Builder
public class IngredientPairingDto {

    RecipeIngredientDto recipeIngredient;
    List<InventoryAllocationDto> allocations;

    public static IngredientPairingDto from(final IngredientPairing pairing) {
        return IngredientPairingDto.builder()
                .recipeIngredient(RecipeIngredientDto.from(pairing.getRecipeIngredient()))
                .allocations(pairing.getAllocations().stream()
                        .map(InventoryAllocationDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public IngredientPairing toModel() {
        return IngredientPairing.builder()
                .recipeIngredient(recipeIngredient.toModel())
                .allocations(allocations == null ? List.of() : allocations.stream()
                        .map(InventoryAllocationDto::toModel)
                        .collect(Collectors.toList()))
                .build();
    }
}
