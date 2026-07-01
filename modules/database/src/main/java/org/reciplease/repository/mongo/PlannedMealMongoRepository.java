package org.reciplease.repository.mongo;

import org.reciplease.model.PlannedMealDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlannedMealMongoRepository extends MongoRepository<PlannedMealDocument, String> {
    List<PlannedMealDocument> findByHouseIdAndDateBetween(String houseId, LocalDate start, LocalDate end);

    List<PlannedMealDocument> findByHouseIdAndRecipeId(String houseId, String recipeId);

    List<PlannedMealDocument> findByHouseIdAndItemsIngredientName(String houseId, String ingredientName);

    boolean existsByHouseIdAndDateAndName(String houseId, LocalDate date, String name);
}
