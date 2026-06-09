package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Recipe;

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

    public static RecipeDto from(final Recipe recipe) {
        return RecipeDto.builder()
                .recipeId(recipe.id())
                .name(recipe.name())
                .description(recipe.description())
                .steps(recipe.steps())
                .ingredients(recipe.recipeIngredients().stream()
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
