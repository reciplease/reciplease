package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.IngredientPairing;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.PlannedRecipeRepository;
import org.reciplease.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannedRecipeService {
    private final PlannedRecipeRepository plannedRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Plans a recipe for a date, pairing recipe ingredients with chosen inventory items. Each
     * referenced inventory item is validated and its barcode snapshotted so future plans of the
     * same recipe can suggest matching items.
     */
    public PlannedRecipe plan(final String recipeId, final LocalDate date, final List<IngredientPairing> pairings) {
        final var recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));

        final var resolvedPairings = pairings.stream()
                .map(this::resolvePairing)
                .collect(Collectors.toList());

        return plannedRecipeRepository.save(PlannedRecipe.builder()
                .recipe(recipe)
                .date(date)
                .pairings(resolvedPairings)
                .build());
    }

    private IngredientPairing resolvePairing(final IngredientPairing pairing) {
        final var allocations = pairing.getAllocations().stream()
                .map(allocation -> {
                    final var item = inventoryRepository.findById(allocation.getInventoryItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Inventory item does not exist"));
                    return InventoryAllocation.builder()
                            .inventoryItemId(item.getId())
                            .barcode(item.getBarcode())
                            .amount(allocation.getAmount())
                            .build();
                })
                .collect(Collectors.toList());

        return IngredientPairing.builder()
                .recipeIngredient(pairing.getRecipeIngredient())
                .allocations(allocations)
                .build();
    }

    /**
     * Suggests inventory items for a recipe ingredient using the history of how this recipe was
     * previously planned: barcodes paired with this ingredient before are matched against current
     * inventory. Falls back to matching the ingredient name when no barcode history is available.
     */
    public List<InventoryItem> suggestInventory(final String recipeId, final String recipeIngredientName) {
        final Set<String> historicBarcodes = plannedRecipeRepository.findByRecipeId(recipeId).stream()
                .flatMap(plannedRecipe -> plannedRecipe.getPairings().stream())
                .filter(pairing -> pairing.getRecipeIngredient().getName().equals(recipeIngredientName))
                .flatMap(pairing -> pairing.getAllocations().stream())
                .map(InventoryAllocation::getBarcode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final List<InventoryItem> byBarcode = historicBarcodes.stream()
                .flatMap(barcode -> inventoryRepository.findByBarcode(barcode).stream())
                .collect(Collectors.toList());

        if (!byBarcode.isEmpty()) {
            return distinctById(byBarcode);
        }

        return distinctById(inventoryRepository.findByName(recipeIngredientName));
    }

    private List<InventoryItem> distinctById(final List<InventoryItem> items) {
        final Map<String, InventoryItem> byId = new LinkedHashMap<>();
        items.forEach(item -> byId.putIfAbsent(item.getId(), item));
        return List.copyOf(byId.values());
    }
}
