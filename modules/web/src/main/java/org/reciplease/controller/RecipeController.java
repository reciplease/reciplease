package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.configuration.OptionalHouseHeader;
import org.reciplease.configuration.RequiresHouseHeader;
import org.reciplease.dto.PublicRecipeDto;
import org.reciplease.dto.RecipeDto;
import org.reciplease.dto.RecipeIngredientDto;
import org.reciplease.dto.UserSummaryDto;
import org.reciplease.model.Recipe;
import org.reciplease.repository.UserRepository;
import org.reciplease.service.RecipeService;
import org.reciplease.service.request.AddIngredient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final HouseAccess houseAccess;
    private final UserRepository userRepository;

    @GetMapping("{uuid}")
    @OptionalHouseHeader
    public ResponseEntity<RecipeDto> findById(@PathVariable final String uuid) {
        final var optionalRecipe =
                recipeService.findVisibleById(uuid, activeHouseId()).map(this::toDto);
        return ResponseEntity.of(optionalRecipe);
    }

    @GetMapping
    @OptionalHouseHeader
    public ResponseEntity<List<RecipeDto>> findAll() {
        final var recipes = recipeService.findVisibleTo(activeHouseId()).stream()
                .map(this::toDto)
                .collect(toList());
        return ResponseEntity.status(HttpStatus.OK).body(recipes);
    }

    @PostMapping
    @HouseOwner
    @RequiresHouseHeader
    @Operation(operationId = "createRecipe")
    public ResponseEntity<RecipeDto> create(@Valid @RequestBody final PublicRecipeDto recipeDto) {
        final Recipe recipe = recipeService.create(houseAccess.requireHouseId(), recipeDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(recipe));
    }

    @PutMapping("{uuid}")
    @HouseOwner
    @RequiresHouseHeader
    @Operation(operationId = "updateRecipe")
    public ResponseEntity<RecipeDto> update(
            @PathVariable final String uuid, @Valid @RequestBody final PublicRecipeDto recipeDto) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var updated = recipeService.update(uuid, recipeDto.toEntity());
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("{uuid}")
    @HouseOwner
    @RequiresHouseHeader
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        recipeService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{uuid}/ingredients")
    @HouseOwner
    @RequiresHouseHeader
    @Operation(operationId = "addRecipeIngredient")
    public ResponseEntity<Set<RecipeIngredientDto>> addIngredient(
            @PathVariable final String uuid, @Valid @RequestBody final AddIngredient addIngredient) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var recipeIngredients = recipeService.addIngredient(uuid, addIngredient).stream()
                .map(RecipeIngredientDto::from)
                .collect(Collectors.toSet());

        return ResponseEntity.status(HttpStatus.CREATED).body(recipeIngredients);
    }

    /** Null for unauthenticated callers or callers with no active house — they only see public recipes. */
    private String activeHouseId() {
        return houseAccess.currentHouseIdOrNull();
    }

    /**
     * Includes houseId/createdBy/updatedBy only for callers who are an authenticated
     * member of the recipe's own house — everyone else (including anonymous public
     * browsing) gets the recipe with no house or user info attached.
     */
    private RecipeDto toDto(final Recipe recipe) {
        if (houseAccess.isMember() && houseAccess.belongsToHeaderHouse(recipe)) {
            return RecipeDto.from(recipe, userSummary(recipe.createdBy()), userSummary(recipe.updatedBy()));
        }
        return RecipeDto.from(recipe);
    }

    private UserSummaryDto userSummary(final String userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).map(UserSummaryDto::from).orElse(null);
    }
}
