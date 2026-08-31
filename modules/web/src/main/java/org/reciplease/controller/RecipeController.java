package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final HouseAccess houseAccess;
    private final UserRepository userRepository;

    @GetMapping("{uuid}")
    @Operation(operationId = "findRecipeById")
    public ResponseEntity<RecipeDto> findById(@PathVariable final String uuid) {
        final var optionalRecipe =
                recipeService.findVisibleById(uuid, houseAccess.currentUserId()).map(this::toDto);
        return ResponseEntity.of(optionalRecipe);
    }

    @GetMapping
    @Operation(operationId = "findAllRecipes")
    public ResponseEntity<List<RecipeDto>> findAll() {
        final var recipes = recipeService.findVisibleTo(houseAccess.currentUserId()).stream()
                .map(this::toDto)
                .collect(toList());
        return ResponseEntity.status(HttpStatus.OK).body(recipes);
    }

    @PostMapping
    @PreAuthorize("hasRole('RECIPLEASE')")
    @Operation(operationId = "createRecipe")
    public ResponseEntity<RecipeDto> create(
            @Valid @RequestBody final PublicRecipeDto recipeDto) {
        final Recipe recipe = recipeService.create(houseAccess.currentUserId(), recipeDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(recipe));
    }

    @PutMapping("{uuid}")
    @PreAuthorize("hasRole('RECIPLEASE')")
    @Operation(operationId = "updateRecipe")
    public ResponseEntity<RecipeDto> update(
            @PathVariable final String uuid, @Valid @RequestBody final PublicRecipeDto recipeDto) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty() || !isOwner(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var updated = recipeService.update(uuid, recipeDto.toEntity());
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("{uuid}")
    @PreAuthorize("hasRole('RECIPLEASE')")
    @Operation(operationId = "deleteRecipeById")
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty() || !isOwner(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        recipeService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{uuid}/ingredients")
    @PreAuthorize("hasRole('RECIPLEASE')")
    @Operation(operationId = "addRecipeIngredient")
    public ResponseEntity<Set<RecipeIngredientDto>> addIngredient(
            @PathVariable final String uuid, @Valid @RequestBody final AddIngredient addIngredient) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty() || !isOwner(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var recipeIngredients = recipeService.addIngredient(uuid, addIngredient).stream()
                .map(RecipeIngredientDto::from)
                .collect(Collectors.toSet());

        return ResponseEntity.status(HttpStatus.CREATED).body(recipeIngredients);
    }

    /**
     * True only for the recipe's owner — the sole writer. Everyone else (including members of
     * houses the recipe is shared to via membership) can view public or shared recipes but never
     * edit, delete, or mutate them.
     */
    private boolean isOwner(final Recipe recipe) {
        final var userId = houseAccess.currentUserId();
        return userId != null && userId.equals(recipe.ownerId());
    }

    /**
     * The owner's view (ownerId/createdBy/updatedBy) goes only to the recipe's owner; every other
     * caller — anonymous public browsing, other users, and members of houses the recipe is shared
     * to — gets the read-only public shape with no owner or creator info attached.
     */
    private RecipeDto toDto(final Recipe recipe) {
        if (isOwner(recipe)) {
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
