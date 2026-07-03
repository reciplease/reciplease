package org.reciplease.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A food-eaten entry to register with whatever service is doing consumption logging, via
 * {@link org.reciplease.service.FoodConsumptionLoggerPort#log(FoodConsumption)}.
 * <p>
 * Either {@code identifiedFoodId} is present (the consumer picked a food from the logger's own
 * {@link org.reciplease.service.FoodConsumptionLoggerPort#history(String)}, so the destination
 * service can resolve accurate nutrients itself), or {@code nutrients} is present (the consumer
 * picked a food from a {@link org.reciplease.service.FoodCatalogPort} search/barcode result
 * instead, so nutrients travel alongside the plain display name). Both may be absent for a
 * bare-name log with no known nutrients.
 */
public record FoodConsumption(
        String userId,
        Optional<String> identifiedFoodId,
        String displayName,
        Optional<Nutrients> nutrients,
        MealType mealType,
        double amount,
        LocalDate date) {}
