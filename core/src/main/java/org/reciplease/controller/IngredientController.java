package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("api/ingredients")
@RequiredArgsConstructor
public class IngredientController {
    final IngredientRepository ingredientRepository;

    @PostMapping
    public ResponseEntity<Ingredient> create(@Valid @RequestBody final Ingredient ingredient) {
        final Ingredient savedIngredient = ingredientRepository.save(ingredient);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedIngredient);
    }

    @GetMapping("{id}")
    public ResponseEntity<Ingredient> findById(@PathVariable final String id) {
        final Optional<Ingredient> foundIngredient = ingredientRepository.findById(id);

        if (foundIngredient.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(foundIngredient.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Ingredient>> findAll() {
        final List<Ingredient> ingredients = ingredientRepository.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(ingredients);
    }
}
