package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeEntity;
import org.reciplease.repository.jpa.RecipeJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepository {
    final RecipeJpaRepository recipeJpaRepository;

    @Override
    public List<Recipe> findAll() {
        return recipeJpaRepository.findAll().stream()
                .map(RecipeEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Recipe save(final Recipe recipe) {
        return recipeJpaRepository.save(RecipeEntity.from(recipe)).toModel();
    }

    @Override
    public Optional<Recipe> findByUuid(final UUID uuid) {
        return recipeJpaRepository.findById(uuid).map(RecipeEntity::toModel);
    }
}
