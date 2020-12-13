package org.reciplease.dto;

import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Recipe;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Value
@Builder
public class RecipeDto {
    UUID uuid;
    String name;
    List<RecipeIngredientDto> ingredients;

    public static RecipeDto from(final Recipe recipe) {
        final var ingredients = recipe.getRecipeIngredients().stream()
                .map(RecipeIngredientDto::from)
                .collect(toList());
        return RecipeDto.builder()
                .uuid(recipe.getUuid())
                .name(recipe.getName())
                .ingredients(ingredients)
                .build();
    }
}
