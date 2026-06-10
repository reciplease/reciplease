package org.reciplease.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Recipe;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
@Builder
public class RecipeDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String recipeId;
    String name;
    String description;
    List<String> steps;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Set<RecipeIngredientDto> ingredients;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Instant updatedAt;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Whether the current user owns this recipe and may edit it")
    boolean editable;

    public static RecipeDto from(final Recipe recipe) {
        return from(recipe, null);
    }

    public static RecipeDto from(final Recipe recipe, final String currentUserId) {
        return RecipeDto.builder()
                .recipeId(recipe.id())
                .name(recipe.name())
                .description(recipe.description())
                .steps(recipe.steps())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()))
                .updatedAt(recipe.updatedAt())
                .editable(currentUserId != null && currentUserId.equals(recipe.createdBy()))
                .build();
    }

    public Recipe toEntity() {
        final var builder = Recipe.builder()
                .id(this.recipeId)
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
