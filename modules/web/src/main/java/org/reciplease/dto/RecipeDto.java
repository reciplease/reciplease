package org.reciplease.dto;

import org.reciplease.model.Recipe;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A recipe as seen over the API — either the {@link PublicRecipeDto} view (no house or user
 * info; safe for anonymous browsing and for callers outside the recipe's own house) or the
 * {@link OwnedRecipeDto} view (adds houseId/createdBy/updatedBy, only for callers who are an
 * authenticated member of the recipe's own house — see {@code RecipeController#toDto}).
 * {@code owned} is the wire-level discriminant a client can switch on to narrow which shape
 * it received.
 */
public sealed interface RecipeDto permits PublicRecipeDto, OwnedRecipeDto {

    String getRecipeId();

    boolean isPublic();

    String getName();

    String getDescription();

    String getSourceUrl();

    List<String> getSteps();

    Set<RecipeIngredientDto> getIngredients();

    Instant getUpdatedAt();

    /** True for {@link OwnedRecipeDto} — the discriminant clients narrow the union on. */
    default boolean isOwned() {
        return false;
    }

    /** Public view: no house or user info. */
    static RecipeDto from(final Recipe recipe) {
        return PublicRecipeDto.from(recipe);
    }

    /** Owner view, for callers who belong to the recipe's own house. */
    static RecipeDto from(final Recipe recipe, final UserSummaryDto createdBy, final UserSummaryDto updatedBy) {
        return OwnedRecipeDto.from(recipe, createdBy, updatedBy);
    }

    default Recipe toEntity() {
        final var builder = Recipe.builder()
                .id(getRecipeId())
                .isPublic(isPublic())
                .name(getName())
                .description(getDescription())
                .sourceUrl(getSourceUrl())
                .steps(getSteps());

        if (getIngredients() != null) {
            builder.recipeIngredients(getIngredients().stream()
                    .map(RecipeIngredientDto::toModel)
                    .collect(Collectors.toSet()));
        }

        return builder.build();
    }
}
