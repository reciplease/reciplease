package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Recipe;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@Builder
public class RecipeDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    UUID recipeId;
    String name;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Set<RecipeIngredientDto> ingredients;

    public static RecipeDto from(final Recipe recipe) {
        return RecipeDto.builder()
                .recipeId(recipe.getUuid())
                .name(recipe.getName())
                .ingredients(recipe.getRecipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()))
                .build();
    }

    public Recipe toEntity() {
        return Recipe.builder()
                .uuid(this.recipeId)
                .name(this.name)
                .build();
    }
}
