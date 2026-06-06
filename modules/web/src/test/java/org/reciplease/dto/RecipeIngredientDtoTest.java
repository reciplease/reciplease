package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.RecipeIngredient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RecipeIngredientDtoTest {

    @Test
    @DisplayName("create DTO from entity")
    void fromEntity() {
        final var recipeIngredient = RecipeIngredient.builder()
                .name("Bread")
                .measure("ITEMS")
                .amount(1d)
                .build();

        final var recipeIngredientDto = RecipeIngredientDto.from(recipeIngredient);

        assertThat(recipeIngredientDto.getName(), is(recipeIngredient.getName()));
        assertThat(recipeIngredientDto.getMeasure(), is(recipeIngredient.getMeasure()));
        assertThat(recipeIngredientDto.getAmount(), is(recipeIngredient.getAmount()));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        final var dto = RecipeIngredientDto.builder()
                .name("Bread").measure("ITEMS").amount(2d).build();

        final var model = dto.toModel();

        assertThat(model.getName(), is("Bread"));
        assertThat(model.getMeasure(), is("ITEMS"));
        assertThat(model.getAmount(), is(2d));
    }
}
