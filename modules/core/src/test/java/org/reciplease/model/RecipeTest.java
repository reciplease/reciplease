package org.reciplease.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeTest {
    @Test
    @DisplayName("create with builder")
    void create() {
        var recipe = Recipe.builder().build();

        assertThat(recipe.recipeIngredients(), is(empty()));
    }

    @Test
    @DisplayName("add ingredient")
    void addIngredient() {
        var recipe = Recipe.builder().build();

        recipe.addIngredient("tomato", "ITEMS", 5d);

        assertThat(recipe.recipeIngredients(), hasSize(1));
        assertThat(recipe.recipeIngredients(), contains(new RecipeIngredient("tomato", "ITEMS", 5d)));
    }

    @Test
    @DisplayName("remove ingredient")
    void removeIngredient() {
        var recipe = Recipe.builder().build();

        recipe.addIngredient("bread", "ITEMS", 5d);
        recipe.addIngredient("tomato", "ITEMS", 10d);
        recipe.removeIngredient("tomato");

        assertThat(recipe.recipeIngredients(), contains(new RecipeIngredient("bread", "ITEMS", 5d)));
    }

    @Test
    void fluentApi() {
        var recipe = Recipe.builder().build();

        assertThat(recipe.addIngredient("tomato", "ITEMS", 5d), is(recipe));
        assertThat(recipe.removeIngredient("tomato"), is(recipe));
    }

    @Test
    @DisplayName("upvotedBy defaults to empty")
    void upvotedByDefaultsToEmpty() {
        var recipe = Recipe.builder().build();

        assertThat(recipe.upvoteCount(), is(0));
        assertThat(recipe.isUpvotedBy("user-1"), is(false));
    }

    @Test
    @DisplayName("upvote count and state reflect the upvotedBy set")
    void upvoteCountAndState() {
        var recipe = Recipe.builder().upvotedBy(Set.of("user-1", "user-2")).build();

        assertThat(recipe.upvoteCount(), is(2));
        assertThat(recipe.isUpvotedBy("user-1"), is(true));
        assertThat(recipe.isUpvotedBy("user-3"), is(false));
        assertThat(recipe.isUpvotedBy(null), is(false));
    }
}
