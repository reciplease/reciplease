package org.reciplease.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.reciplease.model.Recipe;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The owner view of a recipe: only returned to an authenticated caller who is a member of
 * the recipe's own house (see {@code RecipeController#toDto}). Carries the house and the
 * users who created/last updated it — never sent to anyone else.
 */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "OwnedRecipe")
public class OwnedRecipeDto implements RecipeDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String recipeId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String houseId;
    @Getter(onMethod_ = @JsonProperty("isPublic"))
    @Builder.Default
    boolean isPublic = false;
    String name;
    String description;
    String sourceUrl;
    List<String> steps;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Set<RecipeIngredientDto> ingredients;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    UserSummaryDto createdBy;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    UserSummaryDto updatedBy;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Instant updatedAt;

    @Override
    public boolean isOwned() {
        return true;
    }

    public static OwnedRecipeDto from(final Recipe recipe, final UserSummaryDto createdBy, final UserSummaryDto updatedBy) {
        return OwnedRecipeDto.builder()
                .recipeId(recipe.id())
                .houseId(recipe.houseId())
                .isPublic(recipe.isPublic())
                .name(recipe.name())
                .description(recipe.description())
                .sourceUrl(recipe.sourceUrl())
                .steps(recipe.steps())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()))
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .updatedAt(recipe.updatedAt())
                .build();
    }
}
