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
 * The public view of a recipe: safe for anonymous/public browsing and for authenticated
 * callers who aren't a member of the recipe's own house. Also the request body shape for
 * create/update — the server derives houseId/createdBy/updatedBy itself, so a client never
 * submits them.
 */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "PublicRecipe")
public class PublicRecipeDto implements RecipeDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String recipeId;
    // Without an explicit name, Jackson's getter-based property naming maps the
    // isPublic() accessor to JSON key "public" on serialization, while the
    // constructor parameter is named "isPublic" — pin both to "isPublic" so
    // round-tripping doesn't silently drop the field or fail deserialization.
    // The override has to live on the generated getter (not just the field) or
    // swagger-core's schema scan picks up both names and emits the property twice.
    @Getter(onMethod_ = @JsonProperty("isPublic"))
    @Builder.Default
    boolean isPublic = false;
    String name;
    String description;
    String sourceUrl;
    List<String> steps;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Set<RecipeIngredientDto> ingredients;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Instant updatedAt;

    public static PublicRecipeDto from(final Recipe recipe) {
        return PublicRecipeDto.builder()
                .recipeId(recipe.id())
                .isPublic(recipe.isPublic())
                .name(recipe.name())
                .description(recipe.description())
                .sourceUrl(recipe.sourceUrl())
                .steps(recipe.steps())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()))
                .updatedAt(recipe.updatedAt())
                .build();
    }
}
