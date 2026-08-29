package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseOwner;
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

/**
 * {@code GET /api/recipes} and {@code GET /api/recipes/{uuid}} here only handle requests that
 * <em>do</em> carry the {@code X-RCPLS-House-Id} header (see the {@code headers} mapping
 * condition below) — the no-header case is
 * {@link org.reciplease.controller.publicapi.PublicRecipeController}. A present header still
 * isn't proof of membership (it could name a house the caller doesn't belong to, or be stale),
 * so {@link #toDto} still checks {@link HouseAccess#isMember()} before enriching the response;
 * it just never needs to handle a missing header.
 */
@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final HouseAccess houseAccess;
    private final UserRepository userRepository;

    @GetMapping(value = "{uuid}", headers = HouseAccess.HOUSE_HEADER)
    @Parameter(
            name = HouseAccess.HOUSE_HEADER,
            in = ParameterIn.HEADER,
            required = true,
            description = "The house this request is scoped to.",
            schema = @Schema(type = "string"))
    public ResponseEntity<RecipeDto> findById(@PathVariable final String uuid) {
        final var optionalRecipe = recipeService
                .findVisibleById(uuid, houseAccess.requireHouseId())
                .map(this::toDto);
        return ResponseEntity.of(optionalRecipe);
    }

    @GetMapping(headers = HouseAccess.HOUSE_HEADER)
    @Parameter(
            name = HouseAccess.HOUSE_HEADER,
            in = ParameterIn.HEADER,
            required = true,
            description = "The house this request is scoped to.",
            schema = @Schema(type = "string"))
    public ResponseEntity<List<RecipeDto>> findAll() {
        final var recipes = recipeService.findVisibleTo(houseAccess.requireHouseId()).stream()
                .map(this::toDto)
                .collect(toList());
        return ResponseEntity.status(HttpStatus.OK).body(recipes);
    }

    @PostMapping
    @HouseOwner
    @Operation(operationId = "createRecipe")
    public ResponseEntity<RecipeDto> create(@Valid @RequestBody final PublicRecipeDto recipeDto) {
        final Recipe recipe = recipeService.create(houseAccess.requireHouseId(), recipeDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(recipe));
    }

    @PutMapping("{uuid}")
    @HouseOwner
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
