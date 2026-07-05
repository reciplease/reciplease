package org.reciplease.repository.mongo;

import org.reciplease.model.PlannedMealDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PlannedMealMongoRepository extends MongoRepository<PlannedMealDocument, String> {
    // Spring Data Mongo's derived "Between" excludes the upper bound (unlike JPA's
    // inclusive Between), which silently dropped meals planned for the last day of
    // a range — e.g. today's meal, when "today" is also the end of the current
    // week. A derived GreaterThanEqual/LessThanEqual pair on the same field isn't
    // supported either (Criteria can't hold two conditions on one field), so this
    // is a manual query with both bounds inclusive.
    @Query("{ 'houseId': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<PlannedMealDocument> findByHouseIdAndDateBetweenInclusive(String houseId, LocalDate start, LocalDate end);

    List<PlannedMealDocument> findByHouseIdAndRecipeId(String houseId, String recipeId);

    List<PlannedMealDocument> findByHouseIdAndItemsIngredientName(String houseId, String ingredientName);

    boolean existsByHouseIdAndDateAndName(String houseId, LocalDate date, String name);

    boolean existsByHouseIdAndDateAndNameAndIdNot(String houseId, LocalDate date, String name, String id);
}
