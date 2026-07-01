package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PlannedMeal;
import org.reciplease.model.Recipe;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "PlannedMeal")
public class PlannedMealDto {

    String houseId;
    String name;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    RecipeDto recipe;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;
    List<PlannedIngredientDto> items;

    /** {@code recipe} is null when the meal has no {@code recipeId}, or the caller doesn't resolve one. */
    public static PlannedMealDto from(final PlannedMeal plannedMeal, final Recipe recipe) {
        return PlannedMealDto.builder()
                .houseId(plannedMeal.houseId())
                .name(plannedMeal.name())
                .recipe(recipe == null ? null : RecipeDto.from(recipe))
                .date(plannedMeal.date())
                .items(plannedMeal.items().stream()
                        .map(PlannedIngredientDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
