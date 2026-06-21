package org.reciplease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PlannedRecipe;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
@Builder
public class PlannedRecipeDto {

    String houseId;
    RecipeDto recipe;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;
    List<IngredientPairingDto> pairings;

    public static PlannedRecipeDto from(final PlannedRecipe plannedRecipe) {
        return PlannedRecipeDto.builder()
                .houseId(plannedRecipe.houseId())
                .recipe(RecipeDto.from(plannedRecipe.recipe()))
                .date(plannedRecipe.date())
                .pairings(plannedRecipe.pairings().stream()
                        .map(IngredientPairingDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
