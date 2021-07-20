package org.reciplease.dto;

import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Measure;
import org.reciplease.model.RecipeIngredient;

import java.util.UUID;

@Value
@Builder
public class RecipeIngredientDto {
    UUID uuid;
    String name;
    Measure measure;
    Double amount;

    public static RecipeIngredientDto from(final RecipeIngredient recipeIngredient) {
        final var ingredient = recipeIngredient.getIngredient();
        return RecipeIngredientDto.builder()
                .uuid(ingredient.getUuid())
                .name(ingredient.getName())
                .measure(ingredient.getMeasure())
                .amount(recipeIngredient.getAmount())
                .build();
    }
}
