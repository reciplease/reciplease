package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.PlannedRecipeEntity;
import org.reciplease.repository.jpa.PlannedRecipeJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PlannedRecipeRepositoryImpl implements PlannedRecipeRepository {
    private final PlannedRecipeJpaRepository plannedRecipeJpaRepository;

    @Override
    public PlannedRecipe save(PlannedRecipe plannedRecipe) {
        return plannedRecipeJpaRepository.save(PlannedRecipeEntity.from(plannedRecipe)).toModel();
    }

    @Override
    public List<PlannedRecipe> findByDateIsBetween(LocalDate start, LocalDate end) {
        return plannedRecipeJpaRepository.findByDateIsBetween(start, end).stream()
                .map(PlannedRecipeEntity::toModel)
                .collect(Collectors.toList());
    }
}
