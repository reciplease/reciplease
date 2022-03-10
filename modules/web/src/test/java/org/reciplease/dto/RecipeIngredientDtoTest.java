package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RecipeIngredientDtoTest {

    @Test
    @DisplayName("create DTO from entity")
    void fromEntity() {
        final var recipe = Recipe.builder()
                .uuid(UUID.randomUUID())
                .build();
        final var ingredient = Ingredient.builder()
                .uuid(UUID.randomUUID())
                .name("Bread")
                .measure(Measure.ITEMS)
                .build();
        final var recipeIngredient = new RecipeIngredient(ingredient, 1d);

        final var recipeIngredientDto = RecipeIngredientDto.from(recipeIngredient);

        assertThat(recipeIngredientDto.getIngredientUuid(), is(ingredient.getUuid()));
        assertThat(recipeIngredientDto.getName(), is(ingredient.getName()));
        assertThat(recipeIngredientDto.getMeasure(), is(ingredient.getMeasure()));
        assertThat(recipeIngredientDto.getAmount(), is(recipeIngredient.getAmount()));
    }
}
