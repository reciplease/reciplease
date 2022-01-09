package org.reciplease.dto;

import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
