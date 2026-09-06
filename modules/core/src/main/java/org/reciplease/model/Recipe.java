package org.reciplease.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(exclude = {"createdBy", "createdAt", "updatedBy", "updatedAt", "upvotedBy"})
@ToString
@Builder(toBuilder = true)
public final class Recipe implements Audited {
    private final String id;
    private final String createdBy;
    private final Instant createdAt;
    private final String updatedBy;
    private final Instant updatedAt;

    @Builder.Default
    private final boolean isPublic = false;

    private final String name;
    private final String description;
    private final String sourceUrl;

    @Builder.Default
    private final List<String> steps = new ArrayList<>();

    @Builder.Default
    private final Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    @Builder.Default
    private final Set<String> upvotedBy = new HashSet<>();

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

    public boolean isOwnedBy(final String userId) {
        return userId != null && userId.equals(createdBy);
    }

    public int upvoteCount() {
        return upvotedBy.size();
    }

    public boolean isUpvotedBy(final String userId) {
        return userId != null && upvotedBy.contains(userId);
    }

    public static Comparator<Recipe> byUpvotes() {
        return Comparator.comparingInt(Recipe::upvoteCount)
                .reversed()
                .thenComparing(Recipe::createdAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
