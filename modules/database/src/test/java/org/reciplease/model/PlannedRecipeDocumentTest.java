package org.reciplease.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class PlannedRecipeDocumentTest {

    @Test
    @DisplayName("create document from entity")
    void fromModel() {
        var recipe = Recipe.builder().id("recipe-1").name("toast").build();
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var pairing = new IngredientPairing(bread, List.of(new InventoryAllocation("item-1", "111", 2d)));
        var plannedRecipe = new PlannedRecipe("house-1", recipe, LocalDate.of(2026, 6, 6), List.of(pairing));

        var document = PlannedRecipeDocument.from(plannedRecipe);

        assertThat(document.getHouseId(), is("house-1"));
        assertThat(document.getRecipeId(), is("recipe-1"));
        assertThat(document.getDate(), is(LocalDate.of(2026, 6, 6)));
        assertThat(document.getPairings().get(0).toModel(), is(pairing));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var recipe = Recipe.builder().id("recipe-1").name("toast").build();
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var pairing = new IngredientPairing(bread, List.of(new InventoryAllocation("item-1", "111", 2d)));
        var document = PlannedRecipeDocument.builder()
                .id("planned-1")
                .recipeId("recipe-1")
                .date(LocalDate.of(2026, 6, 6))
                .pairings(List.of(IngredientPairingDocument.from(pairing)))
                .build();

        var plannedRecipe = document.toModel(recipe);

        assertThat(plannedRecipe.id(), is("planned-1"));
        assertThat(plannedRecipe.recipe(), is(recipe));
        assertThat(plannedRecipe.pairings(), is(List.of(pairing)));
    }

    @Test
    @DisplayName("round-trips to model when pairings is null")
    void toModelWithNullPairings() {
        var recipe = Recipe.builder().id("recipe-1").name("toast").build();
        var document = PlannedRecipeDocument.builder()
                .id("planned-1")
                .recipeId("recipe-1")
                .date(LocalDate.of(2026, 6, 6))
                .pairings(null)
                .build();

        var plannedRecipe = document.toModel(recipe);

        assertThat(plannedRecipe.pairings(), is(empty()));
    }
}
