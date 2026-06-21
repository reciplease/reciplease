package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.RecipeDto;
import org.reciplease.dto.RecipeIngredientDto;
import org.reciplease.model.Recipe;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final HouseAccess houseAccess;

    @GetMapping("{uuid}")
    public ResponseEntity<RecipeDto> findById(@PathVariable final String uuid) {
        final var optionalRecipe = recipeService.findVisibleById(uuid, activeHouseId())
                .map(RecipeDto::from);
        return ResponseEntity.of(optionalRecipe);
    }

    @GetMapping
    public ResponseEntity<List<RecipeDto>> findAll() {
        final var recipes = recipeService.findVisibleTo(activeHouseId()).stream()
                .map(RecipeDto::from)
                .collect(toList());
        return ResponseEntity.status(HttpStatus.OK).body(recipes);
    }

    @PostMapping
    @HouseOwner
    public ResponseEntity<RecipeDto> create(@RequestBody final RecipeDto recipeDto) {
        final Recipe recipe = recipeService.create(houseAccess.requireHouseId(), recipeDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeDto.from(recipe));
    }

    @PutMapping("{uuid}")
    @HouseOwner
    public ResponseEntity<RecipeDto> update(@PathVariable final String uuid, @RequestBody final RecipeDto recipeDto) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var updated = recipeService.update(uuid, recipeDto.toEntity());
        return ResponseEntity.ok(RecipeDto.from(updated));
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
    public ResponseEntity<Set<RecipeIngredientDto>> addIngredient(@PathVariable final String uuid, @RequestBody final AddIngredient addIngredient) {
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
}
