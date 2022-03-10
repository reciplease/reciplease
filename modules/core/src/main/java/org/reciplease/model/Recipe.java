package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class Recipe extends Identifiable {
    String name;

    Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    public Recipe addIngredient(final Ingredient ingredient, final Double amount) {
        final var recipeItem = new RecipeIngredient(ingredient, amount);
        recipeIngredients.add(recipeItem);
        return this;
    }

    public Recipe removeIngredient(final Ingredient ingredient) {
        recipeIngredients.removeIf(item -> item.getIngredient().equals(ingredient));
        return this;
    }
}
