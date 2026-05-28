package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Recipe;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RecipeDtoTest {

    @Test
    @DisplayName("create DTO from entity")
    void fromEntity() {
        final var recipe = Recipe.builder()
                .uuid(UUID.randomUUID())
                .name("Toast")
                .description("A staple and classic")
                .steps(List.of("Toast the bread", "Spread butter on toast"))
                .build();

        final var recipeDto = RecipeDto.from(recipe);

        assertThat(recipeDto.getRecipeId(), is(recipe.getUuid()));
        assertThat(recipeDto.getName(), is(recipe.getName()));
        assertThat(recipeDto.getDescription(), is(recipe.getDescription()));
        assertThat(recipeDto.getSteps(), is(recipe.getSteps()));
    }

    @Test
    @DisplayName("description and steps default to null when not set")
    void fromEntityDefaults() {
        final var recipe = Recipe.builder()
                .uuid(UUID.randomUUID())
                .name("Toast")
                .build();

        final var recipeDto = RecipeDto.from(recipe);

        assertThat(recipeDto.getDescription(), is((String) null));
    }
}
