package org.reciplease.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.RecipeRepository;
import org.reciplease.service.request.AddIngredient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final HouseRepository houseRepository;

    public Optional<Recipe> findById(final String id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> findAll() {
        final var all = recipeRepository.findAll();
        log.info("Recipes: {}", all);
        return all;
    }

    public List<Recipe> findVisibleTo(final String viewerId) {
        return recipeRepository.findVisibleTo(visibleOwnerIds(viewerId));
    }

    public Optional<Recipe> findVisibleById(final String id, final String viewerId) {
        return recipeRepository.findVisibleById(id, visibleOwnerIds(viewerId));
    }

    public Recipe create(final String userId, final Recipe recipe) {
        return recipeRepository.save(recipe.toBuilder().build());
    }

    public Recipe update(final String id, final Recipe updates) {
        final var existing =
                recipeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));

        final var merged = existing.toBuilder()
                .name(updates.name())
                .description(updates.description())
                .steps(updates.steps())
                .recipeIngredients(updates.recipeIngredients())
                .isPublic(updates.isPublic())
                .build();

        return recipeRepository.save(merged);
    }

    public void deleteById(final String id) {
        recipeRepository.deleteById(id);
    }

    public Set<RecipeIngredient> addIngredient(final String recipeId, final AddIngredient addIngredient) {
        final var recipe = recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));

        recipe.addIngredient(addIngredient.getName(), addIngredient.getMeasure(), addIngredient.getAmount());

        final var savedRecipe = recipeRepository.save(recipe);

        return savedRecipe.recipeIngredients();
    }

    private Set<String> visibleOwnerIds(final String viewerId) {
        final var ownerIds = new HashSet<String>();
        if (viewerId == null) {
            return ownerIds;
        }
        ownerIds.add(viewerId);
        houseRepository
                .findAllForUser(viewerId)
                .forEach(house -> houseRepository.members(house.id()).forEach(member -> ownerIds.add(member.userId())));
        return ownerIds;
    }
}
