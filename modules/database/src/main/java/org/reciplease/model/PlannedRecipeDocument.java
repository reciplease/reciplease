package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("planned_recipes")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlannedRecipeDocument {

    @Id
    private String id;
    private String recipeId;
    private LocalDate date;

    public static PlannedRecipeDocument from(final PlannedRecipe plannedRecipe) {
        return PlannedRecipeDocument.builder()
                .recipeId(plannedRecipe.getRecipe().getId())
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
