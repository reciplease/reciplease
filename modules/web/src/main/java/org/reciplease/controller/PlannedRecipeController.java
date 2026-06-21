package org.reciplease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseMember;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.IngredientPairingDto;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.dto.PlanRecipeRequest;
import org.reciplease.dto.PlannedRecipeDto;
import org.reciplease.model.IngredientPairing;
import org.reciplease.service.PlannedRecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api/planned-recipes")
@RequiredArgsConstructor
public class PlannedRecipeController {

    private final PlannedRecipeService plannedRecipeService;
    private final HouseAccess houseAccess;

    @PostMapping
    @HouseOwner
    public ResponseEntity<PlannedRecipeDto> plan(@Valid @RequestBody final PlanRecipeRequest request) {
        final List<IngredientPairing> pairings = request.getPairings() == null ? List.of()
                : request.getPairings().stream()
                        .map(IngredientPairingDto::toModel)
                        .collect(toList());

        final var plannedRecipe = plannedRecipeService.plan(houseAccess.requireHouseId(), request.getRecipeId(), request.getDate(), pairings);

        return ResponseEntity.status(HttpStatus.CREATED).body(PlannedRecipeDto.from(plannedRecipe));
    }

    @GetMapping("{recipeId}/suggestions")
    @HouseMember
    public ResponseEntity<List<InventoryItemDto>> suggestInventory(@PathVariable final String recipeId,
                                                                   @RequestParam final String ingredient) {
        final List<InventoryItemDto> suggestions = plannedRecipeService.suggestInventory(houseAccess.requireHouseId(), recipeId, ingredient).stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(suggestions);
    }
}
