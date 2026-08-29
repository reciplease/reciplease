package org.reciplease.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.Measure;
import org.reciplease.model.PantryAllocation;
import org.reciplease.model.PantryItem;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.model.ShoppingList;
import org.reciplease.repository.PantryRepository;
import org.reciplease.repository.PlannedMealRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ShoppingListService {
    private final PlannedMealRepository plannedMealRepository;
    private final PantryRepository pantryRepository;

    /**
     * Builds a shopping list for a date range: ingredients with the same name and a convertible
     * measure (e.g. {@code g}/{@code kg}, or {@code tbsp}/{@code cup}) are combined by converting
     * each to its unit family's base measure ({@link Measure#GRAM} for mass, {@link
     * Measure#MILLILITRE} for volume, {@link Measure#ITEM} for count) before summing desired and
     * pantry-covered amounts. Measures that aren't recognized (or that belong to different
     * families, e.g. grams and millilitres) are never combined. Only ingredients with an unmet
     * amount left (gap &gt; 0) are included — fully-allocated ingredients are already owned and
     * don't need buying.
     */
    public ShoppingList generateShoppingList(final String houseId, final LocalDate start, final LocalDate end) {
        var plannedIngredients = plannedMealRepository.findByDateIsBetween(houseId, start, end).stream()
                .flatMap(meal -> meal.items().stream())
                .collect(Collectors.toList());

        var remainingByPantryItemId = remainingByPantryItemId(plannedIngredients);

        var gaps =
                plannedIngredients.stream()
                        .collect(Collectors.groupingBy(item -> keyFor(item.ingredient())))
                        .entrySet()
                        .stream()
                        .map(entry -> toGap(entry.getKey(), entry.getValue(), remainingByPantryItemId))
                        .filter(ingredient -> ingredient != null)
                        .sorted(Comparator.comparing(RecipeIngredient::name))
                        .collect(Collectors.toList());

        return new ShoppingList(gaps);
    }

    /** Current {@code remaining} per allocated pantry item id — missing/deleted items count as 0. */
    private Map<String, Double> remainingByPantryItemId(final List<PlannedIngredient> plannedIngredients) {
        var ids = plannedIngredients.stream()
                .flatMap(item -> item.allocations().stream())
                .map(allocation -> allocation.pantryItemId())
                .collect(Collectors.toSet());

        return pantryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PantryItem::id, PantryItem::remaining));
    }

    private RecipeIngredient toGap(
            final Key key,
            final List<PlannedIngredient> plannedIngredients,
            final Map<String, Double> remainingByPantryItemId) {
        var desired =
                plannedIngredients.stream().mapToDouble(this::toBaseAmount).sum();
        var covered = plannedIngredients.stream()
                .mapToDouble(item -> item.allocations().stream()
                                .mapToDouble(allocation -> coveredAmount(allocation, remainingByPantryItemId))
                                .sum()
                        * conversionFactor(item.ingredient().measure()))
                .sum();

        var gap = desired - covered;
        return gap > 0 ? new RecipeIngredient(key.name(), key.displayMeasure(), gap) : null;
    }

    /** Caps a snapshotted allocation by however much of the source item is actually still left. */
    private double coveredAmount(final PantryAllocation allocation, final Map<String, Double> remainingByPantryItemId) {
        var remaining = remainingByPantryItemId.getOrDefault(allocation.pantryItemId(), 0d);
        return Math.max(0, Math.min(allocation.amount(), remaining));
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
                .map(measure -> new Key(
                        ingredient.name(),
                        measure.getFamily().name(),
                        Measure.baseMeasureFor(measure.getFamily()).getMeasureId()))
                .orElseGet(() -> new Key(ingredient.name(), "RAW:" + ingredient.measure(), ingredient.measure()));
    }

    private record Key(String name, String unit, String displayMeasure) {}
}
