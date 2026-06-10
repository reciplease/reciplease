package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.RecipeDto;
import org.reciplease.dto.RecipeIngredientDto;
import org.reciplease.model.Recipe;
import org.reciplease.service.RecipeService;
import org.reciplease.service.request.AddIngredient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @GetMapping("{uuid}")
    public ResponseEntity<RecipeDto> findById(@PathVariable final String uuid) {
        final var currentUserId = currentUserId();
        final var optionalRecipe = recipeService.findById(uuid)
                .map(recipe -> RecipeDto.from(recipe, currentUserId));
        return ResponseEntity.of(optionalRecipe);
    }

    @GetMapping
    public ResponseEntity<List<RecipeDto>> findAll() {
        final var currentUserId = currentUserId();
        final var recipes = recipeService.findAll().stream()
                .map(recipe -> RecipeDto.from(recipe, currentUserId))
                .collect(toList());
        return ResponseEntity.status(HttpStatus.OK).body(recipes);
    }

    @PostMapping
    public ResponseEntity<RecipeDto> create(@RequestBody final RecipeDto recipeDto) {
        final Recipe recipe = recipeService.create(recipeDto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeDto.from(recipe, currentUserId()));
    }

    @PutMapping("{uuid}")
    public ResponseEntity<RecipeDto> update(@PathVariable final String uuid, @RequestBody final RecipeDto recipeDto) {
        final var existing = recipeService.findById(uuid);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final var currentUserId = currentUserId();
        if (currentUserId == null || !currentUserId.equals(existing.get().createdBy())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final var updated = recipeService.update(uuid, recipeDto.toEntity());
        return ResponseEntity.ok(RecipeDto.from(updated, currentUserId));
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        recipeService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{uuid}/ingredients")
    public ResponseEntity<Set<RecipeIngredientDto>> addIngredient(@PathVariable final String uuid, @RequestBody final AddIngredient addIngredient) {
        final var recipeIngredients = recipeService.addIngredient(uuid, addIngredient).stream()
                .map(RecipeIngredientDto::from)
                .collect(Collectors.toSet());

        return ResponseEntity.status(HttpStatus.CREATED).body(recipeIngredients);
    }

    private String currentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                ? authentication.getName()
                : null;
    }
}
