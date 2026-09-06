package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.CurrentHouse;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseMember;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.PlanMealRequest;
import org.reciplease.dto.PlannedIngredientDto;
import org.reciplease.dto.PlannedMealDto;
import org.reciplease.dto.ShoppingListDto;
import org.reciplease.dto.SuggestedPantryItemDto;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.service.PlannedMealService;
import org.reciplease.service.RecipeService;
import org.reciplease.service.ShoppingListService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/planned-meals")
@Tag(name = "Planned Meals")
@RequiredArgsConstructor
public class PlannedMealController {

    private final PlannedMealService plannedMealService;
    private final ShoppingListService shoppingListService;
    private final RecipeService recipeService;
    private final HouseAccess houseAccess;

    @PostMapping
    @HouseOwner
    @Operation(operationId = "planMeal")
    public ResponseEntity<PlannedMealDto> plan(
            @CurrentHouse final String houseId, @Valid @RequestBody final PlanMealRequest request) {
        final List<PlannedIngredient> items = request.getItems() == null
                ? List.of()
                : request.getItems().stream().map(PlannedIngredientDto::toModel).collect(toList());

        final var plannedMeal =
                plannedMealService.plan(houseId, request.getRecipeId(), request.getName(), request.getDate(), items);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(plannedMeal));
    }

    @GetMapping("{uuid}")
    @HouseMember
    @Operation(operationId = "findPlannedMealById")
    public ResponseEntity<PlannedMealDto> findById(@PathVariable final String uuid) {
        final var meal = plannedMealService.findById(uuid).filter(houseAccess::belongsToHeaderHouse);

        return meal.map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("{uuid}")
    @HouseOwner
    @Operation(operationId = "updatePlannedMeal")
    public ResponseEntity<PlannedMealDto> update(
            @PathVariable final String uuid, @Valid @RequestBody final PlanMealRequest request) {
        final var existing = plannedMealService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final List<PlannedIngredient> items = request.getItems() == null
                ? List.of()
                : request.getItems().stream().map(PlannedIngredientDto::toModel).collect(toList());

        final var updated =
                plannedMealService.update(uuid, request.getRecipeId(), request.getName(), request.getDate(), items);

        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("{uuid}")
    @HouseOwner
    @Operation(operationId = "deletePlannedMealById")
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        final var existing = plannedMealService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        plannedMealService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{uuid}/eaten")
    @HouseOwner
    @Operation(operationId = "markPlannedMealEaten")
    public ResponseEntity<PlannedMealDto> markEaten(@PathVariable final String uuid) {
        final var existing = plannedMealService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var meal = plannedMealService.markEaten(uuid);
        return ResponseEntity.ok(toDto(meal));
    }

    @GetMapping("suggestions")
    @HouseMember
    @Operation(operationId = "suggestPantryItemsForPlannedMeal")
    public ResponseEntity<List<SuggestedPantryItemDto>> suggestPantryItems(
            @CurrentHouse final String houseId,
            @RequestParam(required = false) final String recipeId,
            @RequestParam final String ingredient,
            @RequestParam(required = false) final String excludeMealId) {
        final var suggestionsWithAvailable =
                plannedMealService.suggestPantryItemsWithAvailable(houseId, recipeId, ingredient, excludeMealId);

        final List<SuggestedPantryItemDto> suggestions = suggestionsWithAvailable.entrySet().stream()
                .map(entry -> SuggestedPantryItemDto.from(entry.getKey(), entry.getValue()))
                .collect(toList());

        return ResponseEntity.ok(suggestions);
    }

    @GetMapping
    @HouseMember
    @Operation(operationId = "findPlannedMealsByDateRange")
    public ResponseEntity<List<PlannedMealDto>> findByDateRange(
            @CurrentHouse final String houseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        final List<PlannedMealDto> meals = plannedMealService.findByDateIsBetween(houseId, start, end).stream()
                .map(this::toDto)
                .collect(toList());

        return ResponseEntity.ok(meals);
    }

    @GetMapping("shopping-list")
    @HouseMember
    @Operation(operationId = "findShoppingList")
    public ResponseEntity<ShoppingListDto> shoppingList(
            @CurrentHouse final String houseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        final var shoppingList = shoppingListService.generateShoppingList(houseId, start, end);

        return ResponseEntity.ok(ShoppingListDto.from(shoppingList));
    }

    private PlannedMealDto toDto(final PlannedMeal plannedMeal) {
        final var recipe = plannedMeal.recipeId() == null
                ? null
                : recipeService.findById(plannedMeal.recipeId()).orElse(null);
        return PlannedMealDto.from(plannedMeal, recipe);
    }
}
