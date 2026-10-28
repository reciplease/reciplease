package org.reciplease.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer servings;

    @Builder.Default
    private List<String> steps = new ArrayList<>();

    private List<TableCellDocument> cells;

    @Builder.Default
    private List<RecipeIngredientDocument> ingredients = new ArrayList<>();

    @Builder.Default
    private Set<String> upvotedBy = new HashSet<>();

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private Instant updatedAt;

    public static RecipeDocument from(final Recipe recipe) {
        final var builder =
                switch (recipe.method()) {
                    case ListMethod list -> RecipeDocument.builder().steps(list.steps());
                    case TableMethod table ->
                        RecipeDocument.builder()
                                .steps(null)
                                .cells(table.cells().stream()
                                        .map(TableCellDocument::from)
                                        .toList());
                };
        return builder.id(recipe.id())
                .createdBy(recipe.createdBy())
                .isPublic(recipe.isPublic())
                .name(recipe.name())
                .description(recipe.description())
                .sourceUrl(recipe.sourceUrl())
                .prepTimeMinutes(recipe.prepTimeMinutes())
                .cookTimeMinutes(recipe.cookTimeMinutes())
                .servings(recipe.servings())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDocument::from)
                        .collect(Collectors.toList()))
                .upvotedBy(recipe.upvotedBy() != null ? recipe.upvotedBy() : new HashSet<>())
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
                .prepTimeMinutes(prepTimeMinutes)
                .cookTimeMinutes(cookTimeMinutes)
                .servings(servings)
                .method(toMethod())
                .upvotedBy(upvotedBy != null ? upvotedBy : new HashSet<>())
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

    private RecipeMethod toMethod() {
        if (cells != null) {
            return new TableMethod(
                    cells.stream().map(TableCellDocument::toModel).toList());
        }
        return new ListMethod(steps);
    }
}
