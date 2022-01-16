package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.IngredientDto;
import org.reciplease.dto.IngredientRequest;
import org.reciplease.model.Ingredient;
import org.reciplease.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/ingredients")
@RequiredArgsConstructor
public class IngredientController {
    final IngredientRepository ingredientRepository;

    @PostMapping
    public ResponseEntity<Ingredient> create(@Valid @RequestBody final IngredientRequest ingredientRequest) {
        final Ingredient savedIngredient = ingredientRepository.save(ingredientRequest.toModel());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedIngredient);
    }

    @GetMapping("{uuid}")
    public ResponseEntity<Ingredient> findById(@PathVariable final UUID uuid) {
        final Optional<Ingredient> foundIngredient = ingredientRepository.findByUuid(uuid);

        return ResponseEntity.of(foundIngredient);
    }

    @GetMapping
    public ResponseEntity<List<IngredientDto>> findAll() {
        final List<IngredientDto> ingredients = ingredientRepository.findAll().stream()
            .map(IngredientDto::from)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(ingredients);
    }

    @GetMapping("/search/{searchName}")
    public ResponseEntity<List<IngredientDto>> searchByName(@PathVariable final String searchName) {
        final List<IngredientDto> matchingIngredients = ingredientRepository.searchByName(searchName).stream()
            .map(IngredientDto::from)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(matchingIngredients);
    }
}
