package org.reciplease.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.reciplease.model.PlannedMeal;

public interface PlannedMealRepository {
    PlannedMeal save(PlannedMeal plannedMeal);

    Optional<PlannedMeal> findById(String id);

    void deleteById(String id);

    List<PlannedMeal> findByDateIsBetween(String houseId, LocalDate start, LocalDate end);

    List<PlannedMeal> findByRecipeId(String houseId, String recipeId);

    List<PlannedMeal> findByIngredientName(String houseId, String ingredientName);

    List<PlannedMeal> findByHouseId(String houseId);

    boolean existsByHouseIdAndDateAndName(String houseId, LocalDate date, String name);

    boolean existsByHouseIdAndDateAndNameAndIdNot(String houseId, LocalDate date, String name, String id);
}
