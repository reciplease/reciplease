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

import java.time.Clock;
import java.time.Instant;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class PlannedMealServiceTest {
    private static final String HOUSE_ID = "house-1";
    private static final Instant NOW = Instant.parse("2026-06-06T12:00:00Z");

    @Mock
    private PlannedMealRepository plannedMealRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private Clock clock;

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

    @Test
    @DisplayName("delegates lookup by id to the repository")
    void findById() {
        var meal = new PlannedMeal(HOUSE_ID, recipe.id(), "Dinner", date, List.of());
        when(plannedMealRepository.findById("meal-1")).thenReturn(Optional.of(meal));

        assertThat(plannedMealService.findById("meal-1"), is(Optional.of(meal)));
    }

    @Test
    @DisplayName("delegates deletion by id to the repository")
    void deleteById() {
        plannedMealService.deleteById("meal-1");

        verify(plannedMealRepository).deleteById("meal-1");
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
    class Update {
        @Test
        @DisplayName("re-resolves items and saves the meal under its existing id")
        void updateResolvesItemsAndKeepsId() {
            var existing = new PlannedMeal("meal-1", "owner", HOUSE_ID, recipe.id(), "Dinner", date, List.of(),
                    Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"), null);
            var item = new InventoryItem("item-1", null, HOUSE_ID, "bread", "ITEMS", 2d, date, "111");

            when(plannedMealRepository.findById("meal-1")).thenReturn(Optional.of(existing));
            when(recipeRepository.findById(recipe.id())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("item-1")).thenReturn(Optional.of(item));
            when(plannedMealRepository.existsByHouseIdAndDateAndNameAndIdNot(HOUSE_ID, date, "Dinner", "meal-1")).thenReturn(false);
            when(plannedMealRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var plannedIngredient = new PlannedIngredient(bread, List.of(new InventoryAllocation("item-1", null, 2d)));

            var updated = plannedMealService.update("meal-1", recipe.id(), "Dinner", date, List.of(plannedIngredient));

            assertThat(updated.id(), is("meal-1"));
            assertThat(updated.createdBy(), is("owner"));
            assertThat(updated.items().get(0).allocations().get(0).barcode(), is("111"));
        }

        @Test
        void shouldFail_mealNotFound() {
            when(plannedMealRepository.findById("ghost")).thenReturn(Optional.empty());

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.update("ghost", null, "Dinner", date, List.of()));

            assertThat(exception.getMessage(), is("Planned meal does not exist"));
        }

        @Test
        @DisplayName("fails when the recipe id given doesn't exist")
        void shouldFail_recipeNotFound() {
            var existing = new PlannedMeal("meal-1", null, HOUSE_ID, null, "Dinner", date, List.of(), null, null, null);
            when(plannedMealRepository.findById("meal-1")).thenReturn(Optional.of(existing));
            when(recipeRepository.findById("ghost-recipe")).thenReturn(Optional.empty());

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.update("meal-1", "ghost-recipe", "Dinner", date, List.of()));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }

        @Test
        @DisplayName("fails when renaming to a name already used by another meal that day")
        void shouldFail_nameClash() {
            var existing = new PlannedMeal("meal-1", null, HOUSE_ID, null, "Dinner", date, List.of(), null, null, null);
            when(plannedMealRepository.findById("meal-1")).thenReturn(Optional.of(existing));
            when(plannedMealRepository.existsByHouseIdAndDateAndNameAndIdNot(HOUSE_ID, date, "Lunch", "meal-1")).thenReturn(true);

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.update("meal-1", null, "Lunch", date, List.of()));

            assertThat(exception.getMessage(), is("A meal named 'Lunch' is already planned for this date"));
        }
    }

    @Nested
    class MarkEaten {
        @Test
        @DisplayName("consumes every allocation across every item in the meal and records eatenAt")
        void consumesAllAllocations() {
            var itemA = new PlannedIngredient(bread, List.of(new InventoryAllocation("a", "111", 2d)));
            var rice = new RecipeIngredient("rice", "GRAMS", 200d);
            var itemB = new PlannedIngredient(rice, List.of(
                    new InventoryAllocation("b", "222", 100d),
                    new InventoryAllocation("c", "333", 100d)));
            var meal = new PlannedMeal("meal-1", null, HOUSE_ID, recipe.id(), "Dinner", date, List.of(itemA, itemB), null, null, null);

            when(plannedMealRepository.findById("meal-1")).thenReturn(Optional.of(meal));
            when(clock.instant()).thenReturn(NOW);
            when(plannedMealRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = plannedMealService.markEaten("meal-1");

            verify(inventoryService).consume("a", 2d);
            verify(inventoryService).consume("b", 100d);
            verify(inventoryService).consume("c", 100d);
            assertThat(result.eatenAt(), is(NOW));
        }

        @Test
        @DisplayName("does nothing for ingredients with no allocation")
        void skipsUnallocatedIngredients() {
            var item = new PlannedIngredient(bread, List.of());
            var meal = new PlannedMeal("meal-1", null, HOUSE_ID, null, "Dinner", date, List.of(item), null, null, null);

            when(plannedMealRepository.findById("meal-1")).thenReturn(Optional.of(meal));
            when(clock.instant()).thenReturn(NOW);
            when(plannedMealRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            plannedMealService.markEaten("meal-1");

            verify(inventoryService, never()).consume(any(), any());
        }

        @Test
        void shouldFail_mealNotFound() {
            when(plannedMealRepository.findById("ghost")).thenReturn(Optional.empty());

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.markEaten("ghost"));

            assertThat(exception.getMessage(), is("Planned meal does not exist"));
        }

        @Test
        @DisplayName("fails when the meal has already been marked eaten, so allocations aren't double-consumed")
        void shouldFail_alreadyEaten() {
            var meal = new PlannedMeal("meal-1", null, HOUSE_ID, null, "Dinner", date, List.of(), null, null, NOW);

            when(plannedMealRepository.findById("meal-1")).thenReturn(Optional.of(meal));

            var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedMealService.markEaten("meal-1"));

            assertThat(exception.getMessage(), is("Meal has already been marked as eaten"));
            verify(inventoryService, never()).consume(any(), any());
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
