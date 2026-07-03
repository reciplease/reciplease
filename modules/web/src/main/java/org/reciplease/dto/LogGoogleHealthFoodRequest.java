package org.reciplease.dto;

import java.time.LocalDate;

/**
 * Body of {@code POST /api/google-health/foods/log}. Exactly one of {@code foodId} (a food from
 * the user's Google Health history — logged as an identified food) or {@code nutrients} (a food
 * from a catalog search/barcode lookup — logged anonymously by name, with these macros attached)
 * is expected to be set; both may be absent for a bare-name log with no known macros.
 */
public record LogGoogleHealthFoodRequest(
        String foodId,
        String foodDisplayName,
        String mealType,
        double amount,
        LocalDate date,
        NutrientsDto nutrients) {

    public record NutrientsDto(Double energyKcal, Double proteinG, Double fatG, Double carbohydrateG) {}
}
