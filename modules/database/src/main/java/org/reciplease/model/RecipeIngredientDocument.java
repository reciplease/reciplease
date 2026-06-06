package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientDocument {

    private String name;
    private String measure;
    private Double amount;

    public static RecipeIngredientDocument from(final RecipeIngredient recipeIngredient) {
        return RecipeIngredientDocument.builder()
                .name(recipeIngredient.getName())
                .measure(recipeIngredient.getMeasure())
                .amount(recipeIngredient.getAmount())
                .build();
    }

    public RecipeIngredient toModel() {
        return RecipeIngredient.builder()
                .name(name)
                .measure(measure)
                .amount(amount)
                .build();
    }
}
