package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Document("recipes")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDocument {

    @Id
    private String id;
    private String name;
    private String description;
    @Builder.Default
    private List<String> steps = new ArrayList<>();
    @Builder.Default
    private List<RecipeIngredientDocument> ingredients = new ArrayList<>();
    @CreatedBy
    private String createdBy;

    public static RecipeDocument from(final Recipe recipe) {
        return RecipeDocument.builder()
                .id(recipe.id())
                .name(recipe.name())
                .description(recipe.description())
                .steps(recipe.steps() != null ? recipe.steps() : new ArrayList<>())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDocument::from)
                        .collect(Collectors.toList()))
                .createdBy(recipe.createdBy())
                .build();
    }

    public Recipe toModel() {
        var recipe = Recipe.builder()
                .id(id)
                .name(name)
                .description(description)
                .steps(steps != null ? steps : new ArrayList<>())
                .createdBy(createdBy)
                .build();
        if (ingredients != null) {
            ingredients.stream()
                    .map(RecipeIngredientDocument::toModel)
                    .forEach(recipe::addIngredient);
        }
        return recipe;
    }
}
