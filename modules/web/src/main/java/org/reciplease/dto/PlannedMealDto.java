package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PlannedMeal;
import org.reciplease.model.Recipe;
import org.springframework.format.annotation.DateTimeFormat;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "PlannedMeal")
public class PlannedMealDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String plannedMealId;

    @Schema(requiredMode = REQUIRED)
    String houseId;

    @Schema(requiredMode = REQUIRED)
    String name;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    RecipeDto recipe;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(requiredMode = REQUIRED)
    LocalDate date;

    @Schema(requiredMode = REQUIRED)
    List<PlannedIngredientDto> items;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    Instant eatenAt;

    /** {@code recipe} is null when the meal has no {@code recipeId}, or the caller doesn't resolve one. */
    public static PlannedMealDto from(final PlannedMeal plannedMeal, final Recipe recipe) {
        return PlannedMealDto.builder()
                .plannedMealId(plannedMeal.id())
                .houseId(plannedMeal.houseId())
                .name(plannedMeal.name())
                .recipe(recipe == null ? null : RecipeDto.from(recipe))
                .date(plannedMeal.date())
                .items(plannedMeal.items().stream()
                        .map(PlannedIngredientDto::from)
                        .collect(Collectors.toList()))
                .eatenAt(plannedMeal.eatenAt())
                .build();
    }
}
