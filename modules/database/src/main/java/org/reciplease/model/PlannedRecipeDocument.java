package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
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
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public static PlannedRecipeDocument from(final PlannedRecipe plannedRecipe) {
        return PlannedRecipeDocument.builder()
                .id(plannedRecipe.id())
                .recipeId(plannedRecipe.recipe().id())
                .date(plannedRecipe.date())
                .pairings(plannedRecipe.pairings().stream()
                        .map(IngredientPairingDocument::from)
                        .collect(Collectors.toList()))
                .createdBy(plannedRecipe.createdBy())
                .createdAt(plannedRecipe.createdAt())
                .updatedAt(plannedRecipe.updatedAt())
                .build();
    }

    public PlannedRecipe toModel(final Recipe recipe) {
        var resolvedPairings = pairings == null ? List.<IngredientPairing>of() : pairings.stream()
                .map(IngredientPairingDocument::toModel)
                .collect(Collectors.toList());
        return new PlannedRecipe(id, createdBy, recipe, date, resolvedPairings, createdAt, updatedAt);
    }
}
