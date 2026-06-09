package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Builder.Default
    private List<IngredientPairingDocument> pairings = new ArrayList<>();
    @CreatedBy
    private String createdBy;

    public static PlannedRecipeDocument from(final PlannedRecipe plannedRecipe) {
        return PlannedRecipeDocument.builder()
                .recipeId(plannedRecipe.recipe().id())
                .date(plannedRecipe.date())
                .pairings(plannedRecipe.pairings().stream()
                        .map(IngredientPairingDocument::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public PlannedRecipe toModel(final Recipe recipe) {
        var resolvedPairings = pairings == null ? List.<IngredientPairing>of() : pairings.stream()
                .map(IngredientPairingDocument::toModel)
                .collect(Collectors.toList());
        return new PlannedRecipe(recipe, date, resolvedPairings);
    }
}
