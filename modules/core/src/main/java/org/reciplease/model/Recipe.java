package org.reciplease.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(exclude = {"createdBy", "createdAt", "updatedBy", "updatedAt"})
@ToString
@Builder(toBuilder = true)
public final class Recipe implements Audited, HouseScoped {
    private final String id;
    private final String createdBy;
    private final Instant createdAt;
    private final String updatedBy;
    private final Instant updatedAt;
    private final String houseId;
    @Builder.Default
    private final boolean isPublic = false;
    private final String name;
    private final String description;
    private final String sourceUrl;
    @Builder.Default
    private final List<String> steps = new ArrayList<>();
    @Builder.Default
    private final Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    public Recipe addIngredient(final String name, final String measure, final Double amount) {
        return addIngredient(new RecipeIngredient(name, measure, amount));
    }

    public Recipe addIngredient(final RecipeIngredient recipeIngredient) {
        recipeIngredients.add(recipeIngredient);
        return this;
    }

    public Recipe removeIngredient(final String name) {
        recipeIngredients.removeIf(item -> item.name().equals(name));
        return this;
    }
}
