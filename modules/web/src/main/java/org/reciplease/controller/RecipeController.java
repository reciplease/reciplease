package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.RecipeDto;
import org.reciplease.dto.RecipeIngredientDto;
import org.reciplease.model.Recipe;
import org.reciplease.service.RecipeService;
import org.reciplease.service.request.AddIngredient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping("{uuid}")
    public ResponseEntity<RecipeDto> findById(@PathVariable final UUID uuid) {
        final var optionalRecipe = recipeService.findById(uuid)
                .map(RecipeDto::from);
        return ResponseEntity.of(optionalRecipe);
    }

    @GetMapping
    public ResponseEntity<List<RecipeDto>> findAll() {
        final var recipes = recipeService.findAll().stream()
                .map(RecipeDto::from)
                .collect(toList());
        return ResponseEntity.status(HttpStatus.OK).body(recipes);
    }

    @PostMapping
    public ResponseEntity<RecipeDto> create(@RequestBody final RecipeDto recipeDto) {
        Recipe recipe = recipeService.create(recipeDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeDto.from(recipe));
    }

    @PutMapping("{uuid}/ingredients")
    public ResponseEntity<Set<RecipeIngredientDto>> addIngredient(@PathVariable final UUID uuid, @RequestBody final AddIngredient addIngredient) {
        final var recipeIngredients = recipeService.addIngredient(uuid, addIngredient).stream()
                .map(RecipeIngredientDto::from)
                .collect(Collectors.toSet());

        return ResponseEntity.status(HttpStatus.CREATED).body(recipeIngredients);
    }
}
