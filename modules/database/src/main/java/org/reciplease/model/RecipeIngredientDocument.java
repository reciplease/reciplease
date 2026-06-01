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

    private String ingredientId;
    private String name;
    private String measure;
    private Double amount;

    public static RecipeIngredientDocument from(final RecipeIngredient recipeIngredient) {
        return RecipeIngredientDocument.builder()
                .ingredientId(recipeIngredient.getIngredient().getId())
                .name(recipeIngredient.getIngredient().getName())
                .measure(recipeIngredient.getIngredient().getMeasure())
                .amount(recipeIngredient.getAmount())
                .build();
    }

    public RecipeIngredient toModel() {
        final Ingredient ingredient = Ingredient.builder()
                .id(ingredientId)
                .name(name)
                .measure(measure)
                .build();
        return new RecipeIngredient(ingredient, amount);
    }
}
