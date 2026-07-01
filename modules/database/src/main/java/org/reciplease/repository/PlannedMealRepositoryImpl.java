package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PlannedMeal;
import org.reciplease.model.PlannedMealDocument;
import org.reciplease.repository.mongo.PlannedMealMongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PlannedMealRepositoryImpl implements PlannedMealRepository {
    private final PlannedMealMongoRepository plannedMealMongoRepository;

    @Override
    public PlannedMeal save(final PlannedMeal plannedMeal) {
        final var saved = plannedMealMongoRepository.save(PlannedMealDocument.from(plannedMeal));
        return saved.toModel();
    }

    @Override
    public List<PlannedMeal> findByDateIsBetween(final String houseId, final LocalDate start, final LocalDate end) {
        return plannedMealMongoRepository.findByHouseIdAndDateBetween(houseId, start, end).stream()
                .map(PlannedMealDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlannedMeal> findByRecipeId(final String houseId, final String recipeId) {
        return plannedMealMongoRepository.findByHouseIdAndRecipeId(houseId, recipeId).stream()
                .map(PlannedMealDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlannedMeal> findByIngredientName(final String houseId, final String ingredientName) {
        return plannedMealMongoRepository.findByHouseIdAndItemsIngredientName(houseId, ingredientName).stream()
                .map(PlannedMealDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByHouseIdAndDateAndName(final String houseId, final LocalDate date, final String name) {
        return plannedMealMongoRepository.existsByHouseIdAndDateAndName(houseId, date, name);
    }
}
