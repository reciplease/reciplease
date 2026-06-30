package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.IngredientPairing;

import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "IngredientPairing")
public class IngredientPairingDto {

    RecipeIngredientDto recipeIngredient;
    List<InventoryAllocationDto> allocations;

    public static IngredientPairingDto from(final IngredientPairing pairing) {
        return IngredientPairingDto.builder()
                .recipeIngredient(RecipeIngredientDto.from(pairing.recipeIngredient()))
                .allocations(pairing.allocations().stream()
                        .map(InventoryAllocationDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public IngredientPairing toModel() {
        return new IngredientPairing(recipeIngredient.toModel(),
                allocations == null ? List.of() : allocations.stream()
                        .map(InventoryAllocationDto::toModel)
                        .collect(Collectors.toList()));
    }
}
