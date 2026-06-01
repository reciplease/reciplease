package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Recipe;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Builder
public class RecipeDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String recipeId;
    String name;
    String description;
    List<String> steps;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Set<RecipeIngredientDto> ingredients;

    public static RecipeDto from(final Recipe recipe) {
        return RecipeDto.builder()
                .recipeId(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .steps(recipe.getSteps())
                .ingredients(recipe.getRecipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()))
                .build();
    }

    public Recipe toEntity() {
        return Recipe.builder()
                .id(this.recipeId)
                .name(this.name)
                .description(this.description)
                .steps(this.steps)
                .build();
    }
}
