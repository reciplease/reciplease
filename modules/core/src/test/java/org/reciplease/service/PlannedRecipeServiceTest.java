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
        bread = RecipeIngredient.builder().name("bread").measure("ITEMS").amount(4d).build();
        recipe = Recipe.builder().id(UUID.randomUUID().toString()).name("toast").build()
                .addIngredient(bread);
    }

    @Nested
    class Plan {
        @Test
        @DisplayName("snapshots inventory barcodes onto the saved plan")
        void planSnapshotsBarcode() {
            final var item = InventoryItem.builder()
                    .id("item-1").name("bread").measure("ITEMS").amount(2d)
                    .expiration(date).barcode("111").build();

            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("item-1")).thenReturn(Optional.of(item));
            when(plannedRecipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            final var pairing = IngredientPairing.builder()
                    .recipeIngredient(bread)
                    // amount snapshotted from request; barcode should be filled from the item
                    .allocation(InventoryAllocation.builder().inventoryItemId("item-1").amount(2d).build())
                    .build();

            final PlannedRecipe planned = plannedRecipeService.plan(recipe.getId(), date, List.of(pairing));

            final InventoryAllocation saved = planned.getPairings().get(0).getAllocations().get(0);
            assertThat(saved.getBarcode(), is("111"));
            assertThat(saved.getAmount(), is(2d));
        }

        @Test
        @DisplayName("supports multiple inventory items for one ingredient")
        void planAllowsMultipleAllocations() {
            final var itemA = InventoryItem.builder().id("a").name("bread").measure("ITEMS")
                    .amount(2d).expiration(date).barcode("111").build();
            final var itemB = InventoryItem.builder().id("b").name("bread").measure("ITEMS")
                    .amount(2d).expiration(date).barcode("222").build();

            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("a")).thenReturn(Optional.of(itemA));
            when(inventoryRepository.findById("b")).thenReturn(Optional.of(itemB));
            when(plannedRecipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            final var pairing = IngredientPairing.builder()
                    .recipeIngredient(bread)
                    .allocation(InventoryAllocation.builder().inventoryItemId("a").amount(2d).build())
                    .allocation(InventoryAllocation.builder().inventoryItemId("b").amount(2d).build())
                    .build();

            final PlannedRecipe planned = plannedRecipeService.plan(recipe.getId(), date, List.of(pairing));

            assertThat(planned.getPairings().get(0).getAllocations(), contains(
                    hasAllocation("a", "111", 2d),
                    hasAllocation("b", "222", 2d)));
        }

        @Test
        void failsWhenRecipeMissing() {
            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.empty());

            final var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedRecipeService.plan(recipe.getId(), date, List.of()));

            assertThat(exception.getMessage(), is("Recipe does not exist"));
        }

        @Test
        void failsWhenInventoryItemMissing() {
            when(recipeRepository.findById(recipe.getId())).thenReturn(Optional.of(recipe));
            when(inventoryRepository.findById("ghost")).thenReturn(Optional.empty());

            final var pairing = IngredientPairing.builder()
                    .recipeIngredient(bread)
                    .allocation(InventoryAllocation.builder().inventoryItemId("ghost").amount(1d).build())
                    .build();

            final var exception = assertThrows(IllegalArgumentException.class,
                    () -> plannedRecipeService.plan(recipe.getId(), date, List.of(pairing)));

            assertThat(exception.getMessage(), is("Inventory item does not exist"));
        }
    }

    @Nested
    class Suggest {
        @Test
        @DisplayName("suggests current inventory matching barcodes paired before")
        void suggestsByHistoricBarcode() {
            final var historicPlan = PlannedRecipe.builder()
                    .recipe(recipe).date(date)
                    .pairing(IngredientPairing.builder()
                            .recipeIngredient(bread)
                            .allocation(InventoryAllocation.builder()
                                    .inventoryItemId("old").barcode("111").amount(2d).build())
                            .build())
                    .build();
            final var current = InventoryItem.builder().id("new").name("bread").measure("ITEMS")
                    .amount(5d).expiration(date).barcode("111").build();

            when(plannedRecipeRepository.findByRecipeId(recipe.getId())).thenReturn(List.of(historicPlan));
            when(inventoryRepository.findByBarcode("111")).thenReturn(List.of(current));

            final List<InventoryItem> suggestions = plannedRecipeService.suggestInventory(recipe.getId(), "bread");

            assertThat(suggestions, contains(current));
        }

        @Test
        @DisplayName("falls back to name match when no barcode history")
        void fallsBackToName() {
            when(plannedRecipeRepository.findByRecipeId(recipe.getId())).thenReturn(List.of());
            final var byName = InventoryItem.builder().id("n").name("bread").measure("ITEMS")
                    .amount(1d).expiration(date).build();
            when(inventoryRepository.findByName("bread")).thenReturn(List.of(byName));

            final List<InventoryItem> suggestions = plannedRecipeService.suggestInventory(recipe.getId(), "bread");

            assertThat(suggestions, contains(byName));
        }

        @Test
        void emptyWhenNothingMatches() {
            when(plannedRecipeRepository.findByRecipeId(recipe.getId())).thenReturn(List.of());
            when(inventoryRepository.findByName("bread")).thenReturn(List.of());

            assertThat(plannedRecipeService.suggestInventory(recipe.getId(), "bread"), is(empty()));
        }
    }

    private static org.hamcrest.Matcher<InventoryAllocation> hasAllocation(final String id, final String barcode, final Double amount) {
        return org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.hasProperty("inventoryItemId", is(id)),
                org.hamcrest.Matchers.hasProperty("barcode", is(barcode)),
                org.hamcrest.Matchers.hasProperty("amount", is(amount)));
    }
}
