package org.reciplease.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class Recipe extends Identifiable {
    String name;
    String description;

    @Builder.Default
    List<String> steps = new ArrayList<>();

    Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    public Recipe addIngredient(final String name, final String measure, final Double amount) {
        return addIngredient(RecipeIngredient.builder()
                .name(name)
                .measure(measure)
                .amount(amount)
                .build());
    }

    public Recipe addIngredient(final RecipeIngredient recipeIngredient) {
        recipeIngredients.add(recipeIngredient);
        return this;
    }

    public Recipe removeIngredient(final String name) {
        recipeIngredients.removeIf(item -> item.getName().equals(name));
        return this;
    }
}
