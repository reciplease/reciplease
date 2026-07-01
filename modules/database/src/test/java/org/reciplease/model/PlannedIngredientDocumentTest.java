package org.reciplease.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class PlannedIngredientDocumentTest {

    @Test
    @DisplayName("create document from entity")
    void fromModel() {
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var item = new PlannedIngredient(bread, java.util.List.of(new InventoryAllocation("item-1", "111", 2d)));

        var document = PlannedIngredientDocument.from(item);

        assertThat(document.getIngredient().toModel(), is(bread));
        assertThat(document.getAllocations().get(0).toModel(), is(new InventoryAllocation("item-1", "111", 2d)));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var document = PlannedIngredientDocument.builder()
                .ingredient(RecipeIngredientDocument.from(new RecipeIngredient("bread", "ITEMS", 2d)))
                .allocations(java.util.List.of(InventoryAllocationDocument.from(new InventoryAllocation("item-1", "111", 2d))))
                .build();

        var item = document.toModel();

        assertThat(item.ingredient(), is(new RecipeIngredient("bread", "ITEMS", 2d)));
        assertThat(item.allocations(), is(java.util.List.of(new InventoryAllocation("item-1", "111", 2d))));
    }

    @Test
    @DisplayName("round-trips to model when allocations is null")
    void toModelWithNullAllocations() {
        var document = PlannedIngredientDocument.builder()
                .ingredient(RecipeIngredientDocument.from(new RecipeIngredient("bread", "ITEMS", 2d)))
                .allocations(null)
                .build();

        var item = document.toModel();

        assertThat(item.allocations(), is(empty()));
    }
}
