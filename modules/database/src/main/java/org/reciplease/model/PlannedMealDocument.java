package org.reciplease.model;

import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("planned_meals")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlannedMealDocument {

    @Id
    private String id;

    private String houseId;
    private String recipeId;
    private String name;
    private LocalDate date;

    @Builder.Default
    private List<PlannedIngredientDocument> items = new ArrayList<>();

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant eatenAt;

    public static PlannedMealDocument from(final PlannedMeal plannedMeal) {
        return PlannedMealDocument.builder()
                .id(plannedMeal.id())
                .houseId(plannedMeal.houseId())
                .recipeId(plannedMeal.recipeId())
                .name(plannedMeal.name())
                .date(plannedMeal.date())
                .items(plannedMeal.items().stream()
                        .map(PlannedIngredientDocument::from)
                        .collect(Collectors.toList()))
                .createdBy(plannedMeal.createdBy())
                .createdAt(plannedMeal.createdAt())
                .updatedAt(plannedMeal.updatedAt())
                .eatenAt(plannedMeal.eatenAt())
                .build();
    }

    public PlannedMeal toModel() {
        var resolvedItems = items == null
                ? List.<PlannedIngredient>of()
                : items.stream().map(PlannedIngredientDocument::toModel).collect(Collectors.toList());
        return new PlannedMeal(
                id, createdBy, houseId, recipeId, name, date, resolvedItems, createdAt, updatedAt, eatenAt);
    }
}
