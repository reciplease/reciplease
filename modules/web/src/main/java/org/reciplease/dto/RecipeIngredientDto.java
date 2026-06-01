package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.RecipeIngredient;

@Value
@Builder
public class RecipeIngredientDto {

    String ingredientId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String name;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String measure;
    Double amount;

    public static RecipeIngredientDto from(final RecipeIngredient recipeIngredient) {
        final var ingredient = recipeIngredient.getIngredient();
        return RecipeIngredientDto.builder()
                .ingredientId(ingredient.getId())
                .name(ingredient.getName())
                .measure(ingredient.getMeasure())
                .amount(recipeIngredient.getAmount())
                .build();
    }
}
