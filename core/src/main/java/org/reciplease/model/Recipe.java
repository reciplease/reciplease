package org.reciplease.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Entity
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Recipe extends BaseEntity {
    @ManyToMany
    @JoinTable(name = "RECIPE_TO_ITEM")
    @Builder.Default
    private Set<RecipeItem> recipeItems = new HashSet<>();

    public Recipe addIngredient(final Ingredient ingredient, final Double amount) {
        final var recipeItem = new RecipeItem(this, ingredient, amount);
        recipeItems.add(recipeItem);
        return this;
    }

    public Recipe removeIngredient(final Ingredient ingredient) {
        recipeItems = recipeItems.stream()
                .filter(not(item -> item.getIngredient().equals(ingredient)))
                .collect(toUnmodifiableSet());
        return this;
    }
}
