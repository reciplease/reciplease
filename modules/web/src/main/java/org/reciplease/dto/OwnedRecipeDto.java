package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.reciplease.model.Recipe;

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

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String recipeId;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String houseId;

    @Getter(onMethod_ = @JsonProperty("isPublic"))
    @Builder.Default
    @Schema(requiredMode = REQUIRED)
    boolean isPublic = false;

    @Schema(requiredMode = REQUIRED)
    String name;

    @Schema(requiredMode = REQUIRED)
    String description;

    @Schema(requiredMode = REQUIRED)
    String sourceUrl;

    @Schema(requiredMode = REQUIRED)
    List<String> steps;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    Set<RecipeIngredientDto> ingredients;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    UserSummaryDto createdBy;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    UserSummaryDto updatedBy;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    Instant updatedAt;

    // The EXISTING_PROPERTY discriminant for the RecipeDto polymorphic union — see
    // RecipeDto's @JsonTypeInfo and the matching override on PublicRecipeDto.
    @Override
    @JsonProperty("owned")
    public boolean isOwned() {
        return true;
    }

    public static OwnedRecipeDto from(
            final Recipe recipe, final UserSummaryDto createdBy, final UserSummaryDto updatedBy) {
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
