package org.reciplease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Value
@AllArgsConstructor
@Builder
public class PlanRecipeRequest {

    @NotNull
    String recipeId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;

    List<IngredientPairingDto> pairings;
}
