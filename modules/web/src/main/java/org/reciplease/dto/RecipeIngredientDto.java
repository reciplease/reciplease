package org.reciplease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Measure;
import org.reciplease.model.RecipeIngredient;

@Value
@AllArgsConstructor
@Builder
public class RecipeIngredientDto {

    String name;
    String measure;
    Double amount;

    public static RecipeIngredientDto from(final RecipeIngredient recipeIngredient) {
        return RecipeIngredientDto.builder()
                .name(recipeIngredient.getName())
                .measure(Measure.normalizeId(recipeIngredient.getMeasure()))
                .amount(recipeIngredient.getAmount())
                .build();
    }

    public RecipeIngredient toModel() {
        return RecipeIngredient.builder()
                .name(name)
                .measure(Measure.normalizeId(measure))
                .amount(amount)
                .build();
    }
}
