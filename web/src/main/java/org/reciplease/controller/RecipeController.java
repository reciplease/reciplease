package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Recipe;
import org.reciplease.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping("{uuid}")
    public ResponseEntity<Recipe> getById(@PathVariable final UUID uuid) {
        final var optionalRecipe = recipeService.get(uuid);

        return ResponseEntity.of(optionalRecipe);
    }
}
