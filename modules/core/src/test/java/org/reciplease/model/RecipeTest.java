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
        final Recipe recipe = Recipe.builder().build();

        recipe.addIngredient("tomato", "ITEMS", 5d);

        assertThat(recipe.getRecipeIngredients(), hasSize(1));
        assertThat(recipe.getRecipeIngredients(), contains(allOf(
                hasProperty("name", is("tomato")),
                hasProperty("measure", is("ITEMS")),
                hasProperty("amount", is(5d))
        )));
    }

    @Test
    @DisplayName("remove ingredient")
    void removeIngredient() {
        final Recipe recipe = Recipe.builder().build();

        recipe.addIngredient("bread", "ITEMS", 5d);
        recipe.addIngredient("tomato", "ITEMS", 10d);
        recipe.removeIngredient("tomato");

        assertThat(recipe.getRecipeIngredients(), contains(hasProperty("name", is("bread"))));
    }

    @Test
    void fluentApi() {
        final Recipe recipe = Recipe.builder().build();

        assertThat(recipe.addIngredient("tomato", "ITEMS", 5d), is(recipe));
        assertThat(recipe.removeIngredient("tomato"), is(recipe));
    }
}
