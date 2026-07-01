package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Measure;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.model.ShoppingList;
import org.reciplease.repository.PlannedMealRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShoppingListService {
    private final PlannedMealRepository plannedMealRepository;

    /**
     * Builds a shopping list for a date range: ingredients with the same name and a convertible
     * measure (e.g. {@code g}/{@code kg}, or {@code tbsp}/{@code cup}) are combined by converting
     * each to its unit family's base measure ({@link Measure#GRAM} for mass, {@link
     * Measure#MILLILITRE} for volume, {@link Measure#ITEM} for count) before summing desired and
     * inventory-covered amounts. Measures that aren't recognized (or that belong to different
     * families, e.g. grams and millilitres) are never combined. Only ingredients with an unmet
     * amount left (gap &gt; 0) are included — fully-allocated ingredients are already owned and
     * don't need buying.
     */
    public ShoppingList generateShoppingList(final String houseId, final LocalDate start, final LocalDate end) {
        var items = plannedMealRepository.findByDateIsBetween(houseId, start, end).stream()
                .flatMap(meal -> meal.items().stream())
                .collect(Collectors.groupingBy(item -> keyFor(item.ingredient())));

        var gaps = items.entrySet().stream()
                .map(entry -> toGap(entry.getKey(), entry.getValue()))
                .filter(ingredient -> ingredient != null)
                .sorted(Comparator.comparing(RecipeIngredient::name))
                .collect(Collectors.toList());

        return new ShoppingList(gaps);
    }

    private RecipeIngredient toGap(final Key key, final List<PlannedIngredient> plannedIngredients) {
        var desired = plannedIngredients.stream().mapToDouble(this::toBaseAmount).sum();
        var covered = plannedIngredients.stream()
                .mapToDouble(item -> item.allocations().stream().mapToDouble(allocation -> allocation.amount()).sum()
                        * conversionFactor(item.ingredient().measure()))
                .sum();

        var gap = desired - covered;
        return gap > 0 ? new RecipeIngredient(key.name(), key.displayMeasure(), gap) : null;
    }

    private double toBaseAmount(final PlannedIngredient item) {
        return item.ingredient().amount() * conversionFactor(item.ingredient().measure());
    }

    /** 1.0 for unrecognized measures, so they're neither scaled up nor down before summing. */
    private double conversionFactor(final String measure) {
        return Measure.fromId(measure).map(m -> m.toBaseUnits(1)).orElse(1d);
    }

    private static Key keyFor(final RecipeIngredient ingredient) {
        return Measure.fromId(ingredient.measure())
                .map(measure -> new Key(ingredient.name(), measure.getFamily().name(),
                        Measure.baseMeasureFor(measure.getFamily()).getMeasureId()))
                .orElseGet(() -> new Key(ingredient.name(), "RAW:" + ingredient.measure(), ingredient.measure()));
    }

    private record Key(String name, String unit, String displayMeasure) {
    }
}
