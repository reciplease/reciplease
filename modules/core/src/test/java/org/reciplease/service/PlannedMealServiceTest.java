package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.PlannedMealRepository;
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
class PlannedMealServiceTest {
    private static final String HOUSE_ID = "house-1";

    @Mock
    private PlannedMealRepository plannedMealRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private PlannedMealService plannedMealService;

    private final LocalDate date = LocalDate.of(2026, 6, 6);

    private Recipe recipe;
    private RecipeIngredient bread;

    @BeforeEach
    void setUp() {
        bread = new RecipeIngredient("bread", "ITEMS", 4d);
        recipe = Recipe.builder().id(UUID.randomUUID().toString()).name("toast").build()
                .addIngredient(bread);
    }

    @Test
    @DisplayName("delegates date-range lookup to the repository")
    void findByDateIsBetween() {
        var meal = new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", date, List.of());
        when(plannedMealRepository.findByDateIsBetween(HOUSE_ID, date, date)).thenReturn(List.of(meal));

        assertThat(plannedMealService.findByDateIsBetween(HOUSE_ID, date, date), contains(meal));
    }

    @Nested
    class Plan {
        @Test
        @DisplayName("snapshots inventory barcodes onto the saved plan")
        void planSnapshotsBarcode() {
            var item = new InventoryItem("item-1", null, HOUSE_ID, "bread", "ITEMS", 2d, date, "111");

            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("item-1")).thenReturn(Optional.of(item));
            when(plannedMealRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var plannedIngredient = new PlannedIngredient(bread,
                    // amount snapshotted from request; barcode should be filled from the item
                    List.of(new InventoryAllocation("item-1", null, 2d)));

            var planned = plannedMealService.plan(HOUSE_ID, recipe.id(), "Dinner", date, List.of(plannedIngredient));

            var saved = planned.items().get(0).allocations().get(0);
            assertThat(saved.barcode(), is("111"));
            assertThat(saved.amount(), is(2d));
        }

        @Test
        @DisplayName("supports multiple inventory items for one ingredient")
        void planAllowsMultipleAllocations() {
            var itemA = new InventoryItem("a", null, HOUSE_ID, "bread", "ITEMS", 2d, date, "111");
            var itemB = new InventoryItem("b", null, HOUSE_ID, "bread", "ITEMS", 2d, date, "222");

            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("a")).thenReturn(Optional.of(itemA));
            when(inventoryRepository.findById("b")).thenReturn(Optional.of(itemB));
            when(plannedMealRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var plannedIngredient = new PlannedIngredient(bread,
                    List.of(new InventoryAllocation("a", null, 2d), new InventoryAllocation("b", null, 2d)));

            var planned = plannedMealService.plan(HOUSE_ID, recipe.id(), "Dinner", date, List.of(plannedIngredient));

            assertThat(planned.items().get(0).allocations(), contains(
                    hasAllocation("a", "111", 2d),
                    hasAllocation("b", "222", 2d)));
        }

        @Test
        @DisplayName("plans a meal without a recipe, using just a name")
        void planSucceedsWithoutRecipe() {
            when(plannedMealRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var planned = plannedMealService.plan(HOUSE_ID, null, "Leftover rice night", date, List.of());

            assertThat(planned.recipeId(), is((String) null));
            assertThat(planned.name(), is("Leftover rice night"));
        }

        @Test
        void failsWhenRecipeIdProvidedButMissing() {
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.empty());

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.plan(HOUSE_ID, recipe.id(), "Dinner", date, List.of()));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }

        @Test
        void failsWhenInventoryItemMissing() {
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("ghost")).thenReturn(Optional.empty());

            var plannedIngredient = new PlannedIngredient(bread, List.of(new InventoryAllocation("ghost", null, 1d)));

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.plan(HOUSE_ID, recipe.id(), "Dinner", date, List.of(plannedIngredient)));

            assertThat(exception.getMessage(), is("Inventory item does not exist"));
        }

        @Test
        @DisplayName("fails when a meal with the same name is already planned for that date")
        void failsWhenNameAlreadyPlannedForDate() {
            when(plannedMealRepository.existsByHouseIdAndDateAndName(HOUSE_ID, date, "Dinner")).thenReturn(true);

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.plan(HOUSE_ID, null, "Dinner", date, List.of()));

            assertThat(exception.getMessage(), is("A meal named 'Dinner' is already planned for this date"));
        }
    }

    @Nested
    class Suggest {
        @Test
        @DisplayName("suggests current inventory matching barcodes paired before, scoped to a recipe")
        void suggestsByHistoricBarcodeForRecipe() {
            var historicPlan = new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", date,
                    List.of(new PlannedIngredient(bread, List.of(new InventoryAllocation("old", "111", 2d)))));
            var current = new InventoryItem("new", null, HOUSE_ID, "bread", "ITEMS", 5d, date, "111");

            when(plannedMealRepository.findByRecipeId(HOUSE_ID, recipe.id())).thenReturn(List.of(historicPlan));
            when(inventoryRepository.findByBarcodeIn(HOUSE_ID, Set.of("111"))).thenReturn(List.of(current));

            var suggestions = plannedMealService.suggestInventory(HOUSE_ID, recipe.id(), "bread");

            assertThat(suggestions, contains(current));
        }

        @Test
        @DisplayName("suggests current inventory matching barcodes paired before, across all meals when no recipe given")
        void suggestsByHistoricBarcodeAcrossAllMeals() {
            var historicPlan = new PlannedMeal(HOUSE_ID, null, "Leftover rice night", date,
                    List.of(new PlannedIngredient(bread, List.of(new InventoryAllocation("old", "111", 2d)))));
            var current = new InventoryItem("new", null, HOUSE_ID, "bread", "ITEMS", 5d, date, "111");

            when(plannedMealRepository.findByIngredientName(HOUSE_ID, "bread")).thenReturn(List.of(historicPlan));
            when(inventoryRepository.findByBarcodeIn(HOUSE_ID, Set.of("111"))).thenReturn(List.of(current));

            var suggestions = plannedMealService.suggestInventory(HOUSE_ID, null, "bread");

            assertThat(suggestions, contains(current));
        }

        @Test
        @DisplayName("falls back to name match when no barcode history")
        void fallsBackToName() {
            when(plannedMealRepository.findByRecipeId(HOUSE_ID, recipe.id())).thenReturn(List.of());
            var byName = new InventoryItem("n", null, HOUSE_ID, "bread", "ITEMS", 1d, date, null);
            when(inventoryRepository.findByName(HOUSE_ID, "bread")).thenReturn(List.of(byName));

            var suggestions = plannedMealService.suggestInventory(HOUSE_ID, recipe.id(), "bread");

            assertThat(suggestions, contains(byName));
        }

        @Test
        void emptyWhenNothingMatches() {
            when(plannedMealRepository.findByRecipeId(HOUSE_ID, recipe.id())).thenReturn(List.of());
            when(inventoryRepository.findByName(HOUSE_ID, "bread")).thenReturn(List.of());

            assertThat(plannedMealService.suggestInventory(HOUSE_ID, recipe.id(), "bread"), is(empty()));
        }
    }

    private static org.hamcrest.Matcher<InventoryAllocation> hasAllocation(final String id, final String barcode, final Double amount) {
        return is(new InventoryAllocation(id, barcode, amount));
    }
}
