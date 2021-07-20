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

    public static RecipeDto from(final Recipe recipe) {
        return RecipeDto.builder()
                .uuid(recipe.getUuid())
                .name(recipe.getName())
                .build();
    }

    public Recipe toEntity() {
        return Recipe.builder()
                .uuid(this.uuid)
                .name(this.name)
                .build();
    }
}
