package org.reciplease.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Recipe;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Carries no per-recipe "editable" flag: the frontend determines edit access from the
 * caller's role on the active house (via {@code GET /api/houses}), not a field repeated
 * on every recipe. The backend still enforces it independently via {@code @HouseOwner}.
 */
@Value
@AllArgsConstructor
@Builder
public class RecipeDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String recipeId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String houseId;
    // Without an explicit name, Jackson's getter-based property naming maps the
    // isPublic() accessor to JSON key "public" on serialization, while the
    // constructor parameter is named "isPublic" — pin both to "isPublic" so
    // round-tripping doesn't silently drop the field or fail deserialization.
    @JsonProperty("isPublic")
    @Builder.Default
    boolean isPublic = false;
    String name;
    String description;
    List<String> steps;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Set<RecipeIngredientDto> ingredients;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Instant updatedAt;

    public static RecipeDto from(final Recipe recipe) {
        return RecipeDto.builder()
                .recipeId(recipe.id())
                .houseId(recipe.houseId())
                .isPublic(recipe.isPublic())
                .name(recipe.name())
                .description(recipe.description())
                .steps(recipe.steps())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()))
                .updatedAt(recipe.updatedAt())
                .build();
    }

    public Recipe toEntity() {
        final var builder = Recipe.builder()
                .id(this.recipeId)
                .isPublic(this.isPublic)
                .name(this.name)
                .description(this.description)
                .steps(this.steps);

        if (this.ingredients != null) {
            builder.recipeIngredients(this.ingredients.stream()
                    .map(RecipeIngredientDto::toModel)
                    .collect(Collectors.toSet()));
        }

        return builder.build();
    }
}
