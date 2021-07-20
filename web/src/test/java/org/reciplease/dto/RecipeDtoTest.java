package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.reciplease.model.Recipe;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RecipeDtoTest {

    @Test
    @DisplayName("create DTO from entity")
    void fromEntity() {
        final var recipe = Recipe.builder()
                .randomUUID()
                .name("Toast")
                .build();

        final var recipeDto = RecipeDto.from(recipe);

        assertThat(recipeDto.getUuid(), is(recipe.getUuid()));
        assertThat(recipeDto.getName(), is(recipe.getName()));
    }
}