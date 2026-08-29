package org.reciplease.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeDocumentTest {

    @Test
    @DisplayName("create document from entity")
    void fromModel() {
        var recipe = Recipe.builder()
                .id("recipe-1")
                .name("toast")
                .description("A staple")
                .steps(List.of("Toast it"))
                .createdBy("user-1")
                .updatedBy("user-2")
                .build()
                .addIngredient("bread", "ITEMS", 2d);

        var document = RecipeDocument.from(recipe);

        assertThat(document.getId(), is("recipe-1"));
        assertThat(document.getName(), is("toast"));
        assertThat(document.getDescription(), is("A staple"));
        assertThat(document.getSteps(), is(List.of("Toast it")));
        assertThat(document.getIngredients().getFirst().toModel(), is(new RecipeIngredient("bread", "ITEMS", 2d)));
        assertThat(document.getCreatedBy(), is("user-1"));
        assertThat(document.getUpdatedBy(), is("user-2"));
    }

    @Test
    @DisplayName("creates document with empty steps when entity steps is null")
    void fromModelWithNullSteps() {
        var recipe = Recipe.builder().id("recipe-1").name("toast").steps(null).build();

        var document = RecipeDocument.from(recipe);

        assertThat(document.getSteps(), is(empty()));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var document = RecipeDocument.builder()
                .id("recipe-1")
                .name("toast")
                .description("A staple")
                .steps(List.of("Toast it"))
                .ingredients(List.of(RecipeIngredientDocument.from(new RecipeIngredient("bread", "ITEMS", 2d))))
                .createdBy("user-1")
                .updatedBy("user-2")
                .build();

        var recipe = document.toModel();

        assertThat(recipe.id(), is("recipe-1"));
        assertThat(recipe.name(), is("toast"));
        assertThat(recipe.description(), is("A staple"));
        assertThat(recipe.steps(), is(List.of("Toast it")));
        assertThat(recipe.recipeIngredients(), contains(new RecipeIngredient("bread", "ITEMS", 2d)));
        assertThat(recipe.createdBy(), is("user-1"));
        assertThat(recipe.updatedBy(), is("user-2"));
    }

    @Test
    @DisplayName("round-trips to model when steps and ingredients are null")
    void toModelWithNullStepsAndIngredients() {
        var document = RecipeDocument.builder()
                .id("recipe-1")
                .name("toast")
                .steps(null)
                .ingredients(null)
                .build();

        var recipe = document.toModel();

        assertThat(recipe.steps(), is(empty()));
        assertThat(recipe.recipeIngredients(), is(empty()));
    }
}
