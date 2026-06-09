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
                .name(recipeIngredient.name())
                .measure(recipeIngredient.measure())
                .amount(recipeIngredient.amount())
                .build();
    }

    public RecipeIngredient toModel() {
        return new RecipeIngredient(name, measure, amount);
    }
}
