package org.reciplease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseMember;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.dto.PlanMealRequest;
import org.reciplease.dto.PlannedIngredientDto;
import org.reciplease.dto.PlannedMealDto;
import org.reciplease.dto.ShoppingListDto;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.service.PlannedMealService;
import org.reciplease.service.RecipeService;
import org.reciplease.service.ShoppingListService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api/planned-meals")
@RequiredArgsConstructor
public class PlannedMealController {

    private final PlannedMealService plannedMealService;
    private final ShoppingListService shoppingListService;
    private final RecipeService recipeService;
    private final HouseAccess houseAccess;

    @PostMapping
    @HouseOwner
    public ResponseEntity<PlannedMealDto> plan(@Valid @RequestBody final PlanMealRequest request) {
        final List<PlannedIngredient> items = request.getItems() == null ? List.of()
                : request.getItems().stream()
                        .map(PlannedIngredientDto::toModel)
                        .collect(toList());

        final var plannedMeal = plannedMealService.plan(houseAccess.requireHouseId(), request.getRecipeId(),
                request.getName(), request.getDate(), items);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(plannedMeal));
    }

    @GetMapping("suggestions")
    @HouseMember
    public ResponseEntity<List<InventoryItemDto>> suggestInventory(@RequestParam(required = false) final String recipeId,
                                                                    @RequestParam final String ingredient) {
        final List<InventoryItemDto> suggestions = plannedMealService.suggestInventory(houseAccess.requireHouseId(), recipeId, ingredient).stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(suggestions);
    }

    @GetMapping
    @HouseMember
    public ResponseEntity<List<PlannedMealDto>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        final List<PlannedMealDto> meals = plannedMealService.findByDateIsBetween(houseAccess.requireHouseId(), start, end).stream()
                .map(this::toDto)
                .collect(toList());

        return ResponseEntity.ok(meals);
    }

    @GetMapping("shopping-list")
    @HouseMember
    public ResponseEntity<ShoppingListDto> shoppingList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate end) {
        final var shoppingList = shoppingListService.generateShoppingList(houseAccess.requireHouseId(), start, end);

        return ResponseEntity.ok(ShoppingListDto.from(shoppingList));
    }

    private PlannedMealDto toDto(final PlannedMeal plannedMeal) {
        final var recipe = plannedMeal.recipeId() == null ? null : recipeService.findById(plannedMeal.recipeId()).orElse(null);
        return PlannedMealDto.from(plannedMeal, recipe);
    }
}
