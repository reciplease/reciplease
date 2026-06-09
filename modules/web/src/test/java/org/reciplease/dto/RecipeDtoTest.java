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
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .description("A staple and classic")
                .steps(List.of("Toast the bread", "Spread butter on toast"))
                .build();

        var recipeDto = RecipeDto.from(recipe);

        assertThat(recipeDto.getRecipeId(), is(recipe.id()));
        assertThat(recipeDto.getName(), is(recipe.name()));
        assertThat(recipeDto.getDescription(), is(recipe.description()));
        assertThat(recipeDto.getSteps(), is(recipe.steps()));
    }

    @Test
    @DisplayName("description and steps default to null when not set")
    void fromEntityDefaults() {
        var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("Toast")
                .build();

        var recipeDto = RecipeDto.from(recipe);

        assertThat(recipeDto.getDescription(), is((String) null));
    }
}
