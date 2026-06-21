package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.IngredientPairing;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.RecipeIngredient;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class IngredientPairingDtoTest {

    @Test
    @DisplayName("create DTO from entity")
    void fromEntity() {
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var pairing = new IngredientPairing(bread, List.of(new InventoryAllocation("item-1", "111", 2d)));

        var dto = IngredientPairingDto.from(pairing);

        assertThat(dto.getRecipeIngredient(), is(RecipeIngredientDto.from(bread)));
        assertThat(dto.getAllocations(), contains(InventoryAllocationDto.from(new InventoryAllocation("item-1", "111", 2d))));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var dto = IngredientPairingDto.builder()
                .recipeIngredient(RecipeIngredientDto.builder().name("bread").measure("item").amount(2d).build())
                .allocations(List.of(InventoryAllocationDto.builder().inventoryItemId("item-1").amount(2d).build()))
                .build();

        var pairing = dto.toModel();

        assertThat(pairing.recipeIngredient(), is(new RecipeIngredient("bread", "item", 2d)));
        assertThat(pairing.allocations(), contains(new InventoryAllocation("item-1", null, 2d)));
    }

    @Test
    @DisplayName("round-trips to model with no allocations")
    void toModelWithoutAllocations() {
        var dto = IngredientPairingDto.builder()
                .recipeIngredient(RecipeIngredientDto.builder().name("bread").measure("item").amount(2d).build())
                .build();

        var pairing = dto.toModel();

        assertThat(pairing.allocations(), is(empty()));
    }
}
