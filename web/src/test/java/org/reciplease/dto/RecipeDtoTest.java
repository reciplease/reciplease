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
        final var ingredient = Ingredient.builder()
                .randomUUID()
                .name("Bread")
                .measure(Measure.ITEMS)
                .build();
        final var recipe = Recipe.builder()
                .randomUUID()
                .name("Toast")
                .build()
                .addIngredient(ingredient, 1d);

        final var recipeDto = RecipeDto.from(recipe);
        final List<RecipeIngredientDto> ingredientDtoList = recipe.getRecipeIngredients().stream()
                .map(RecipeIngredientDto::from)
                .collect(toList());

        assertThat(recipeDto.getUuid(), is(recipe.getUuid()));
        assertThat(recipeDto.getName(), is(recipe.getName()));
        assertThat(recipeDto.getIngredients(), is(ingredientDtoList));
    }
}