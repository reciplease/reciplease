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
        var recipeIngredient = new RecipeIngredient("Bread", "ITEMS", 1d);

        var recipeIngredientDto = RecipeIngredientDto.from(recipeIngredient);

        assertThat(recipeIngredientDto.getName(), is(recipeIngredient.name()));
        // Legacy measure ids are normalized to their short form.
        assertThat(recipeIngredientDto.getMeasure(), is("item"));
        assertThat(recipeIngredientDto.getAmount(), is(recipeIngredient.amount()));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var dto = RecipeIngredientDto.builder()
                .name("Bread").measure("item").amount(2d).build();

        var model = dto.toModel();

        assertThat(model.name(), is("Bread"));
        assertThat(model.measure(), is("item"));
        assertThat(model.amount(), is(2d));
    }
}
