package org.reciplease.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class IngredientPairingDocumentTest {

    @Test
    @DisplayName("create document from entity")
    void fromModel() {
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var pairing = new IngredientPairing(bread, java.util.List.of(new InventoryAllocation("item-1", "111", 2d)));

        var document = IngredientPairingDocument.from(pairing);

        assertThat(document.getRecipeIngredient().toModel(), is(bread));
        assertThat(document.getAllocations().get(0).toModel(), is(new InventoryAllocation("item-1", "111", 2d)));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var document = IngredientPairingDocument.builder()
                .recipeIngredient(RecipeIngredientDocument.from(new RecipeIngredient("bread", "ITEMS", 2d)))
                .allocations(java.util.List.of(InventoryAllocationDocument.from(new InventoryAllocation("item-1", "111", 2d))))
                .build();

        var pairing = document.toModel();

        assertThat(pairing.recipeIngredient(), is(new RecipeIngredient("bread", "ITEMS", 2d)));
        assertThat(pairing.allocations(), is(java.util.List.of(new InventoryAllocation("item-1", "111", 2d))));
    }

    @Test
    @DisplayName("round-trips to model when allocations is null")
    void toModelWithNullAllocations() {
        var document = IngredientPairingDocument.builder()
                .recipeIngredient(RecipeIngredientDocument.from(new RecipeIngredient("bread", "ITEMS", 2d)))
                .allocations(null)
                .build();

        var pairing = document.toModel();

        assertThat(pairing.allocations(), is(empty()));
    }
}
