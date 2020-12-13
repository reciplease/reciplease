package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reciplease.model.Recipe;
import org.reciplease.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public Optional<Recipe> findById(final UUID uuid) {
        return recipeRepository.findById(uuid);
    }

    public List<Recipe> findAll() {
        final var all = recipeRepository.findAll();
        log.info("Recipes: {}", all);
        return all;
    }
}
