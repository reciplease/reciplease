package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Set;

import static java.util.function.Predicate.not;


@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class Recipe extends BaseEntity {
    String name;

    Set<RecipeIngredient> recipeIngredients;

    public Recipe addIngredient(final Ingredient ingredient, final Double amount) {
        final var recipeItem = new RecipeIngredient(this, ingredient, amount);
        recipeIngredients.add(recipeItem);
        return this;
    }

    public Recipe removeIngredient(final Ingredient ingredient) {
        recipeIngredients.removeIf(not(item -> item.getIngredient().equals(ingredient)));
        return this;
    }
}
