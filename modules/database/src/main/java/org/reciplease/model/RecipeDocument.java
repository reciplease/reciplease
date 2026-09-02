package org.reciplease.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("recipes")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDocument {

    @Id
    private String id;

    @Field("public")
    @Builder.Default
    private boolean isPublic = false;

    private String name;
    private String description;
    private String sourceUrl;

    @Builder.Default
    private List<String> steps = new ArrayList<>();

    @Builder.Default
    private List<RecipeIngredientDocument> ingredients = new ArrayList<>();

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private Instant updatedAt;

    public static RecipeDocument from(final Recipe recipe) {
        return RecipeDocument.builder()
                .id(recipe.id())
                .createdBy(recipe.createdBy())
                .isPublic(recipe.isPublic())
                .name(recipe.name())
                .description(recipe.description())
                .sourceUrl(recipe.sourceUrl())
                .steps(recipe.steps() != null ? recipe.steps() : new ArrayList<>())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDocument::from)
                        .collect(Collectors.toList()))
                .createdBy(recipe.createdBy())
                .createdAt(recipe.createdAt())
                .updatedBy(recipe.updatedBy())
                .updatedAt(recipe.updatedAt())
                .build();
    }

    public Recipe toModel() {
        var recipe = Recipe.builder()
                .id(id)
                .isPublic(isPublic)
                .name(name)
                .description(description)
                .sourceUrl(sourceUrl)
                .steps(steps != null ? steps : new ArrayList<>())
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
        if (ingredients != null) {
            ingredients.stream().map(RecipeIngredientDocument::toModel).forEach(recipe::addIngredient);
        }
        return recipe;
    }
}
