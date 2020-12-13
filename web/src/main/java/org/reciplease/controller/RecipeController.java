package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.RecipeDto;
import org.reciplease.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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
}
