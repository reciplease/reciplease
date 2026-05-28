package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document("planned_recipes")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlannedRecipeDocument {

    @Id
    private UUID uuid;
    private UUID recipeId;
    private LocalDate date;

    public static PlannedRecipeDocument from(final PlannedRecipe plannedRecipe) {
        return PlannedRecipeDocument.builder()
                .uuid(UUID.randomUUID())
                .recipeId(plannedRecipe.getRecipe().getUuid())
                .date(plannedRecipe.getDate())
                .build();
    }

    public PlannedRecipe toModel(final Recipe recipe) {
        return PlannedRecipe.builder()
                .recipe(recipe)
                .date(date)
                .build();
    }
}
