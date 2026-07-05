package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.PlannedMealRepository;
import org.reciplease.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannedMealService {
    private final PlannedMealRepository plannedMealRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Plans a meal for a date, optionally linked to a recipe, pairing ingredients with chosen
     * inventory items. Each referenced inventory item is validated and its barcode snapshotted so
     * future plans can suggest matching items. The meal's name must be unique for that date.
     */
    public PlannedMeal plan(final String houseId, final String recipeId, final String name,
                             final LocalDate date, final List<PlannedIngredient> items) {
        if (recipeId != null) {
            recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
        }

        if (plannedMealRepository.existsByHouseIdAndDateAndName(houseId, date, name)) {
            throw new IllegalArgumentException("A meal named '" + name + "' is already planned for this date");
        }

        var resolvedItems = items.stream()
                .map(this::resolveItem)
                .collect(Collectors.toList());

        return plannedMealRepository.save(new PlannedMeal(houseId, recipeId, name, date, resolvedItems));
    }

    public Optional<PlannedMeal> findById(final String id) {
        return plannedMealRepository.findById(id);
    }

    /**
     * Updates an existing planned meal's recipe link, name, date and items, re-resolving
     * item allocations the same way {@link #plan} does. The name-uniqueness check excludes
     * the meal being updated, so keeping the same name/date doesn't clash with itself.
     */
    public PlannedMeal update(final String id, final String recipeId, final String name,
                               final LocalDate date, final List<PlannedIngredient> items) {
        final var existing = plannedMealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planned meal does not exist"));

        if (recipeId != null) {
            recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
        }

        if (plannedMealRepository.existsByHouseIdAndDateAndNameAndIdNot(existing.houseId(), date, name, id)) {
            throw new IllegalArgumentException("A meal named '" + name + "' is already planned for this date");
        }

        var resolvedItems = items.stream()
                .map(this::resolveItem)
                .collect(Collectors.toList());

        var updated = new PlannedMeal(id, existing.createdBy(), existing.houseId(), recipeId, name, date,
                resolvedItems, existing.createdAt(), existing.updatedAt());

        return plannedMealRepository.save(updated);
    }

    private PlannedIngredient resolveItem(final PlannedIngredient item) {
        var allocations = item.allocations().stream()
                .map(allocation -> {
                    var inventoryItem = inventoryRepository.findById(allocation.inventoryItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Inventory item does not exist"));
                    return new InventoryAllocation(inventoryItem.id(), inventoryItem.barcode(), allocation.amount());
                })
                .collect(Collectors.toList());

        return new PlannedIngredient(item.ingredient(), allocations);
    }

    public List<PlannedMeal> findByDateIsBetween(final String houseId, final LocalDate start, final LocalDate end) {
        return plannedMealRepository.findByDateIsBetween(houseId, start, end);
    }

    /**
     * Suggests inventory items for an ingredient using the history of how it was previously
     * planned: barcodes paired with this ingredient before are matched against current inventory.
     * When {@code recipeId} is given, history is scoped to that recipe; otherwise it's searched
     * across all planned meals for the house. Falls back to matching the ingredient name when no
     * barcode history is available.
     */
    public List<InventoryItem> suggestInventory(final String houseId, final String recipeId, final String ingredientName) {
        var historicMeals = recipeId != null
                ? plannedMealRepository.findByRecipeId(houseId, recipeId)
                : plannedMealRepository.findByIngredientName(houseId, ingredientName);

        var historicBarcodes = historicMeals.stream()
                .flatMap(meal -> meal.items().stream())
                .filter(item -> item.ingredient().name().equals(ingredientName))
                .flatMap(item -> item.allocations().stream())
                .map(InventoryAllocation::barcode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var byBarcode = inventoryRepository.findByBarcodeIn(houseId, historicBarcodes);

        if (!byBarcode.isEmpty()) {
            return distinctById(byBarcode);
        }

        return distinctById(inventoryRepository.findByName(houseId, ingredientName));
    }

    private List<InventoryItem> distinctById(final List<InventoryItem> items) {
        Map<String, InventoryItem> byId = new LinkedHashMap<>();
        items.forEach(item -> byId.putIfAbsent(item.id(), item));
        return List.copyOf(byId.values());
    }
}
