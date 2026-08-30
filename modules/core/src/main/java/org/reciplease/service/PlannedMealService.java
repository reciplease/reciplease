package org.reciplease.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.PantryAllocation;
import org.reciplease.model.PantryItem;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.repository.PantryRepository;
import org.reciplease.repository.PlannedMealRepository;
import org.reciplease.repository.RecipeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlannedMealService {
    private final PlannedMealRepository plannedMealRepository;
    private final RecipeRepository recipeRepository;
    private final PantryRepository pantryRepository;
    private final PantryService pantryService;
    private final Clock clock;

    /**
     * Plans a meal for a date, optionally linked to a recipe, pairing ingredients with chosen
     * pantry items. Each referenced pantry item is validated and its barcode snapshotted so
     * future plans can suggest matching items. The meal's name must be unique for that date.
     */
    public PlannedMeal plan(
            final String houseId,
            final String recipeId,
            final String name,
            final LocalDate date,
            final List<PlannedIngredient> items) {
        if (recipeId != null) {
            recipeRepository
                    .findById(recipeId)
                    .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
        }

        if (plannedMealRepository.existsByHouseIdAndDateAndName(houseId, date, name)) {
            throw new IllegalArgumentException("A meal named '" + name + "' is already planned for this date");
        }

        var resolvedItems = items.stream().map(this::resolveItem).collect(Collectors.toList());

        return plannedMealRepository.save(new PlannedMeal(houseId, recipeId, name, date, resolvedItems));
    }

    public Optional<PlannedMeal> findById(final String id) {
        return plannedMealRepository.findById(id);
    }

    public void deleteById(final String id) {
        plannedMealRepository.deleteById(id);
    }

    /**
     * Marks a meal as eaten: a shortcut for consuming every pantry item allocated across
     * all of its ingredients, instead of marking each one as eaten individually. Records
     * {@code eatenAt} so the meal isn't marked eaten twice (which would double-consume
     * its allocations).
     */
    public PlannedMeal markEaten(final String id) {
        final var meal = plannedMealRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planned meal does not exist"));

        if (meal.eatenAt() != null) {
            throw new IllegalArgumentException("Meal has already been marked as eaten");
        }

        meal.items().stream()
                .flatMap(item -> item.allocations().stream())
                .forEach(allocation -> pantryService.consume(allocation.pantryItemId(), allocation.amount()));

        return plannedMealRepository.save(meal.withEatenAt(Instant.now(clock)));
    }

    /**
     * Updates an existing planned meal's recipe link, name, date and items, re-resolving
     * item allocations the same way {@link #plan} does. The name-uniqueness check excludes
     * the meal being updated, so keeping the same name/date doesn't clash with itself.
     */
    public PlannedMeal update(
            final String id,
            final String recipeId,
            final String name,
            final LocalDate date,
            final List<PlannedIngredient> items) {
        final var existing = plannedMealRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planned meal does not exist"));

        if (recipeId != null) {
            recipeRepository
                    .findById(recipeId)
                    .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
        }

        if (plannedMealRepository.existsByHouseIdAndDateAndNameAndIdNot(existing.houseId(), date, name, id)) {
            throw new IllegalArgumentException("A meal named '" + name + "' is already planned for this date");
        }

        var resolvedItems = items.stream().map(this::resolveItem).collect(Collectors.toList());

        var updated = new PlannedMeal(
                id,
                existing.createdBy(),
                existing.houseId(),
                recipeId,
                name,
                date,
                resolvedItems,
                existing.createdAt(),
                existing.updatedAt(),
                existing.eatenAt());

        return plannedMealRepository.save(updated);
    }

    private PlannedIngredient resolveItem(final PlannedIngredient item) {
        var allocations = item.allocations().stream()
                .map(allocation -> {
                    var pantryItem = pantryRepository
                            .findById(allocation.pantryItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Pantry item does not exist"));
                    return new PantryAllocation(pantryItem.id(), pantryItem.barcode(), allocation.amount());
                })
                .collect(Collectors.toList());

        return new PlannedIngredient(item.ingredient(), allocations);
    }

    public List<PlannedMeal> findByDateIsBetween(final String houseId, final LocalDate start, final LocalDate end) {
        return plannedMealRepository.findByDateIsBetween(houseId, start, end);
    }

    /**
     * Suggests pantry items for an ingredient using the history of how it was previously
     * planned: barcodes paired with this ingredient before are matched against current pantry.
     * When {@code recipeId} is given, history is scoped to that recipe; otherwise it's searched
     * across all planned meals for the house. Falls back to matching the ingredient name when no
     * barcode history is available.
     */
    public List<PantryItem> suggestPantryItems(
            final String houseId, final String recipeId, final String ingredientName) {
        var historicMeals = recipeId != null
                ? plannedMealRepository.findByRecipeId(houseId, recipeId)
                : plannedMealRepository.findByIngredientName(houseId, ingredientName);

        var historicBarcodes = historicMeals.stream()
                .flatMap(meal -> meal.items().stream())
                .filter(item -> item.ingredient().name().equals(ingredientName))
                .flatMap(item -> item.allocations().stream())
                .map(PantryAllocation::barcode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var byBarcode = pantryRepository.findByBarcodeIn(houseId, historicBarcodes);

        if (!byBarcode.isEmpty()) {
            return distinctById(byBarcode);
        }

        return distinctById(pantryRepository.findByName(houseId, ingredientName));
    }

    public Map<PantryItem, Double> suggestPantryItemsWithAvailable(
            final String houseId,
            final String recipeId,
            final String ingredientName,
            final String excludeMealId) {
        var suggestedItems = suggestPantryItems(houseId, recipeId, ingredientName);
        var committedElsewhere = committedElsewhere(houseId, ingredientName, excludeMealId);

        return suggestedItems.stream()
                .collect(Collectors.toMap(
                        item -> item,
                        item -> Math.max(0, item.remaining() - committedElsewhere.getOrDefault(item.id(), 0d)),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private Map<String, Double> committedElsewhere(
            final String houseId, final String ingredientName, final String excludeMealId) {
        return plannedMealRepository.findByHouseId(houseId).stream()
                .filter(meal -> !Objects.equals(meal.id(), excludeMealId))
                .flatMap(meal -> meal.items().stream())
                .filter(item -> item.ingredient().name().equals(ingredientName))
                .flatMap(item -> item.allocations().stream())
                .collect(Collectors.toMap(
                        PantryAllocation::pantryItemId, PantryAllocation::amount, Double::sum));
    }

    private List<PantryItem> distinctById(final List<PantryItem> items) {
        Map<String, PantryItem> byId = new LinkedHashMap<>();
        items.forEach(item -> byId.putIfAbsent(item.id(), item));
        return List.copyOf(byId.values());
    }
}
