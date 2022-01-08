package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Measure;
import org.reciplease.model.RecipeIngredient;

import java.util.UUID;

@Value
@Builder
public class RecipeIngredientDto {
    UUID ingredientUuid;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String name;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Measure measure;
    Double amount;

    public static RecipeIngredientDto from(final RecipeIngredient recipeIngredient) {
        final var ingredient = recipeIngredient.getIngredient();
        return RecipeIngredientDto.builder()
                .ingredientUuid(ingredient.getUuid())
                .name(ingredient.getName())
                .measure(ingredient.getMeasure())
                .amount(recipeIngredient.getAmount())
                .build();
    }
}
