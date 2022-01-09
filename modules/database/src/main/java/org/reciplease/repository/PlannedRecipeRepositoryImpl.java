package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PlannedRecipe;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlannedRecipeRepositoryImpl implements PlannedRecipeRepository {
    @Override
    public PlannedRecipe save(PlannedRecipe plannedRecipe) {
        return null;
    }

    @Override
    public List<PlannedRecipe> findByDateIsBetween(LocalDate start, LocalDate end) {
        return null;
    }
}
