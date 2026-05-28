package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientDocument {

    private UUID ingredientId;
    private String name;
    private Measure measure;
    private Double amount;

    public static RecipeIngredientDocument from(final RecipeIngredient recipeIngredient) {
        return RecipeIngredientDocument.builder()
                .ingredientId(recipeIngredient.getIngredient().getUuid())
                .name(recipeIngredient.getIngredient().getName())
                .measure(recipeIngredient.getIngredient().getMeasure())
                .amount(recipeIngredient.getAmount())
                .build();
    }

    public RecipeIngredient toModel() {
        final Ingredient ingredient = Ingredient.builder()
                .uuid(ingredientId)
                .name(name)
                .measure(measure)
                .build();
        return new RecipeIngredient(ingredient, amount);
    }
}
