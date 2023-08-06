package org.reciplease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;


@Value
@Builder
public class IngredientRequest {

    @NotNull
    @NotBlank
    String name;

    @NotNull
    Measure measure;

    public Ingredient toModel() {
        return Ingredient.builder()
                .name(getName())
                .measure(getMeasure())
                .build();
    }
}
