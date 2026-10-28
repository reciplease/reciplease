package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.reciplease.model.Recipe;

@Schema(name = "Recipe")
public record RecipeDto(
        @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
        String recipeId,

        @JsonProperty("isPublic") @Schema(requiredMode = REQUIRED)
        boolean isPublic,

        @Schema(requiredMode = REQUIRED) String name,
        @Schema(requiredMode = REQUIRED) String description,
        @Schema(requiredMode = REQUIRED) String sourceUrl,
        @Schema(nullable = true) Integer prepTimeMinutes,
        @Schema(nullable = true) Integer cookTimeMinutes,
        @Schema(nullable = true) Integer servings,
        @Schema(requiredMode = REQUIRED) RecipeMethodDto method,
        @Schema(requiredMode = REQUIRED) Set<RecipeIngredientDto> ingredients,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
        Instant updatedAt,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
        int upvoteCount,

        @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
        boolean upvotedByCurrentUser,

        @JsonInclude(JsonInclude.Include.NON_NULL) OwnershipDto ownership) {

    public static RecipeDto publicView(final Recipe recipe) {
        return publicView(recipe, null);
    }

    public static RecipeDto publicView(final Recipe recipe, final String viewerId) {
        return new RecipeDto(
                recipe.id(),
                recipe.isPublic(),
                recipe.name(),
                recipe.description(),
                recipe.sourceUrl(),
                recipe.prepTimeMinutes(),
                recipe.cookTimeMinutes(),
                recipe.servings(),
                RecipeMethodDto.from(recipe.method()),
                recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()),
                recipe.updatedAt(),
                recipe.upvoteCount(),
                recipe.isUpvotedBy(viewerId),
                null);
    }

    public static RecipeDto ownerView(
            final Recipe recipe, final UserSummaryDto createdBy, final UserSummaryDto updatedBy) {
        return ownerView(recipe, createdBy, updatedBy, null);
    }

    public static RecipeDto ownerView(
            final Recipe recipe,
            final UserSummaryDto createdBy,
            final UserSummaryDto updatedBy,
            final String viewerId) {
        return new RecipeDto(
                recipe.id(),
                recipe.isPublic(),
                recipe.name(),
                recipe.description(),
                recipe.sourceUrl(),
                recipe.prepTimeMinutes(),
                recipe.cookTimeMinutes(),
                recipe.servings(),
                RecipeMethodDto.from(recipe.method()),
                recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()),
                recipe.updatedAt(),
                recipe.upvoteCount(),
                recipe.isUpvotedBy(viewerId),
                new OwnershipDto(createdBy, updatedBy));
    }
}
