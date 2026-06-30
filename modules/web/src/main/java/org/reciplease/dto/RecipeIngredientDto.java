package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Measure;
import org.reciplease.model.RecipeIngredient;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "RecipeIngredient")
public class RecipeIngredientDto {

    String name;
    String measure;
    Double amount;

    public static RecipeIngredientDto from(final RecipeIngredient recipeIngredient) {
        return RecipeIngredientDto.builder()
                .name(recipeIngredient.name())
                .measure(Measure.normalizeId(recipeIngredient.measure()))
                .amount(recipeIngredient.amount())
                .build();
    }

    public RecipeIngredient toModel() {
        return new RecipeIngredient(name, Measure.normalizeId(measure), amount);
    }
}
