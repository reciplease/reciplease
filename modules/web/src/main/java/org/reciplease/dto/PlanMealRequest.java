package org.reciplease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

@Value
@AllArgsConstructor
@Builder
public class PlanMealRequest {

    String recipeId;

    @NotBlank
    String name;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;

    List<PlannedIngredientDto> items;
}
