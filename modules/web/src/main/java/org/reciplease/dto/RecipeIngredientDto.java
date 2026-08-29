package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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

    @Schema(requiredMode = REQUIRED)
    String name;

    @Schema(requiredMode = REQUIRED)
    String measure;

    @Schema(requiredMode = REQUIRED)
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
