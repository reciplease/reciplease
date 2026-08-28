package org.reciplease.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class PlannedMealDocumentTest {

    @Test
    @DisplayName("create document from entity")
    void fromModel() {
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var item = new PlannedIngredient(bread, List.of(new PantryAllocation("item-1", "111", 2d)));
        var plannedMeal = new PlannedMeal("house-1", "recipe-1", "Dinner", LocalDate.of(2026, 6, 6), List.of(item));

        var document = PlannedMealDocument.from(plannedMeal);

        assertThat(document.getHouseId(), is("house-1"));
        assertThat(document.getRecipeId(), is("recipe-1"));
        assertThat(document.getName(), is("Dinner"));
        assertThat(document.getDate(), is(LocalDate.of(2026, 6, 6)));
        assertThat(document.getItems().getFirst().toModel(), is(item));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var item = new PlannedIngredient(bread, List.of(new PantryAllocation("item-1", "111", 2d)));
        var document = PlannedMealDocument.builder()
                .id("planned-1")
                .recipeId("recipe-1")
                .name("Dinner")
                .date(LocalDate.of(2026, 6, 6))
                .items(List.of(PlannedIngredientDocument.from(item)))
                .build();

        var plannedMeal = document.toModel();

        assertThat(plannedMeal.id(), is("planned-1"));
        assertThat(plannedMeal.recipeId(), is("recipe-1"));
        assertThat(plannedMeal.items(), is(List.of(item)));
    }

    @Test
    @DisplayName("round-trips to model when recipeId is absent")
    void toModelWithoutRecipeId() {
        var document = PlannedMealDocument.builder()
                .id("planned-1")
                .name("Leftover rice night")
                .date(LocalDate.of(2026, 6, 6))
                .build();

        var plannedMeal = document.toModel();

        assertThat(plannedMeal.recipeId(), is(nullValue()));
        assertThat(plannedMeal.name(), is("Leftover rice night"));
    }

    @Test
    @DisplayName("round-trips to model when items is null")
    void toModelWithNullItems() {
        var document = PlannedMealDocument.builder()
                .id("planned-1")
                .recipeId("recipe-1")
                .name("Dinner")
                .date(LocalDate.of(2026, 6, 6))
                .items(null)
                .build();

        var plannedMeal = document.toModel();

        assertThat(plannedMeal.items(), is(empty()));
    }
}
