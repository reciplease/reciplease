package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.IngredientPairing;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.PlannedRecipeRepository;
import org.reciplease.repository.RecipeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MockitoSettings
class PlannedRecipeServiceTest {
    @Mock
    private PlannedRecipeRepository plannedRecipeRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private PlannedRecipeService plannedRecipeService;

    private final LocalDate date = LocalDate.of(2026, 6, 6);

    private Recipe recipe;
    private RecipeIngredient bread;

    @BeforeEach
    void setUp() {
        bread = new RecipeIngredient("bread", "ITEMS", 4d);
        recipe = Recipe.builder().id(UUID.randomUUID().toString()).name("toast").build()
                .addIngredient(bread);
    }

    @Nested
    class Plan {
        @Test
        @DisplayName("snapshots inventory barcodes onto the saved plan")
        void planSnapshotsBarcode() {
            var item = new InventoryItem("item-1", null, "bread", "ITEMS", 2d, date, "111");

            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("item-1")).thenReturn(Optional.of(item));
            when(plannedRecipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var pairing = new IngredientPairing(bread,
                    // amount snapshotted from request; barcode should be filled from the item
                    List.of(new InventoryAllocation("item-1", null, 2d)));

            var planned = plannedRecipeService.plan(recipe.id(), date, List.of(pairing));

            var saved = planned.pairings().get(0).allocations().get(0);
            assertThat(saved.barcode(), is("111"));
            assertThat(saved.amount(), is(2d));
        }

        @Test
        @DisplayName("supports multiple inventory items for one ingredient")
        void planAllowsMultipleAllocations() {
            var itemA = new InventoryItem("a", null, "bread", "ITEMS", 2d, date, "111");
            var itemB = new InventoryItem("b", null, "bread", "ITEMS", 2d, date, "222");

            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("a")).thenReturn(Optional.of(itemA));
            when(inventoryRepository.findById("b")).thenReturn(Optional.of(itemB));
            when(plannedRecipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var pairing = new IngredientPairing(bread,
                    List.of(new InventoryAllocation("a", null, 2d), new InventoryAllocation("b", null, 2d)));

            var planned = plannedRecipeService.plan(recipe.id(), date, List.of(pairing));

            assertThat(planned.pairings().get(0).allocations(), contains(
                    hasAllocation("a", "111", 2d),
                    hasAllocation("b", "222", 2d)));
        }

        @Test
        void failsWhenRecipeMissing() {
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.empty());

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedRecipeService.plan(recipe.id(), date, List.of()));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }

        @Test
        void failsWhenInventoryItemMissing() {
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("ghost")).thenReturn(Optional.empty());

            var pairing = new IngredientPairing(bread, List.of(new InventoryAllocation("ghost", null, 1d)));

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedRecipeService.plan(recipe.id(), date, List.of(pairing)));

            assertThat(exception.getMessage(), is("Inventory item does not exist"));
        }
    }

    @Nested
    class Suggest {
        @Test
        @DisplayName("suggests current inventory matching barcodes paired before")
        void suggestsByHistoricBarcode() {
            var historicPlan = new PlannedRecipe(recipe, date,
                    List.of(new IngredientPairing(bread, List.of(new InventoryAllocation("old", "111", 2d)))));
            var current = new InventoryItem("new", null, "bread", "ITEMS", 5d, date, "111");

            when(plannedRecipeRepository.findByRecipeId(recipe.id())).thenReturn(List.of(historicPlan));
            when(inventoryRepository.findByBarcodeIn(Set.of("111"))).thenReturn(List.of(current));

            var suggestions = plannedRecipeService.suggestInventory(recipe.id(), "bread");

            assertThat(suggestions, contains(current));
        }

        @Test
        @DisplayName("falls back to name match when no barcode history")
        void fallsBackToName() {
            when(plannedRecipeRepository.findByRecipeId(recipe.id())).thenReturn(List.of());
            var byName = new InventoryItem("n", null, "bread", "ITEMS", 1d, date, null);
            when(inventoryRepository.findByName("bread")).thenReturn(List.of(byName));

            var suggestions = plannedRecipeService.suggestInventory(recipe.id(), "bread");

            assertThat(suggestions, contains(byName));
        }

        @Test
        void emptyWhenNothingMatches() {
            when(plannedRecipeRepository.findByRecipeId(recipe.id())).thenReturn(List.of());
            when(inventoryRepository.findByName("bread")).thenReturn(List.of());

            assertThat(plannedRecipeService.suggestInventory(recipe.id(), "bread"), is(empty()));
        }
    }

    private static org.hamcrest.Matcher<InventoryAllocation> hasAllocation(final String id, final String barcode, final Double amount) {
        return is(new InventoryAllocation(id, barcode, amount));
    }
}
