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
        final Recipe recipe = Recipe.builder().build();

        assertThat(recipe.getRecipeIngredients(), is(empty()));
    }

    @Test
    @DisplayName("add ingredient")
    void addIngredient() {
        final Ingredient ingredient = Ingredient.builder()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();

        final Recipe recipe = Recipe.builder()
                .build();

        recipe.addIngredient(ingredient, 5d);

        assertThat(recipe.getRecipeIngredients(), hasSize(1));
        assertThat(recipe.getRecipeIngredients(), contains(allOf(
                hasProperty("ingredient", is(ingredient)),
                hasProperty("amount", is(5d))
        )));
    }

    @Test
    @DisplayName("remove ingredient")
    void removeIngredient() {
        final Ingredient tomato = Ingredient.builder()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();
        final Ingredient bread = Ingredient.builder()
            .name("bread")
            .measure(Measure.ITEMS)
            .build();

        final Recipe recipe = Recipe.builder()
                .build();

        recipe.addIngredient(bread, 5d);
        recipe.addIngredient(tomato, 10d);
        recipe.removeIngredient(tomato);

        assertThat(recipe.getRecipeIngredients(), contains(hasProperty("ingredient", is(bread))));
    }

    @Test
    void fluentApi() {
        final Ingredient ingredient = Ingredient.builder()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();

        final Recipe recipe = Recipe.builder()
                .build();

        assertThat(recipe.addIngredient(ingredient, 5d), is(recipe));
        assertThat(recipe.removeIngredient(ingredient), is(recipe));
    }
}
