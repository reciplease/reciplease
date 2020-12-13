package org.reciplease.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class RecipeTest {
    @Test
    @DisplayName("create with builder")
    void create() {
        final Recipe recipe = Recipe.builder()
                .randomUUID()
                .build();

        assertThat(recipe.getRecipeIngredients(), is(empty()));
    }

    @Test
    @DisplayName("add ingredient")
    void addIngredient() {
        final Ingredient ingredient = Ingredient.builder()
                .randomUUID()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();

        final Recipe recipe = Recipe.builder()
                .randomUUID()
                .build();

        recipe.addIngredient(ingredient, 5d);

        assertThat(recipe.getRecipeIngredients(), hasSize(1));
        assertThat(recipe.getRecipeIngredients(), contains(allOf(
                hasProperty("recipe", is(recipe)),
                hasProperty("ingredient", is(ingredient)),
                hasProperty("amount", is(5d))
        )));
    }

    @Test
    @DisplayName("remove ingredient")
    void removeIngredient() {
        final Ingredient ingredient = Ingredient.builder()
                .randomUUID()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();

        final Recipe recipe = Recipe.builder()
                .randomUUID()
                .build();

        recipe.addIngredient(ingredient, 5d);
        recipe.removeIngredient(ingredient);

        assertThat(recipe.getRecipeIngredients(), is(empty()));
    }

    @Test
    void fluentApi() {
        final Ingredient ingredient = Ingredient.builder()
                .randomUUID()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();

        final Recipe recipe = Recipe.builder()
                .randomUUID()
                .build();

        assertThat(recipe.addIngredient(ingredient, 5d), is(recipe));
        assertThat(recipe.removeIngredient(ingredient), is(recipe));
    }
}