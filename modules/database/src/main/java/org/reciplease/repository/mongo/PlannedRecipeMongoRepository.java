package org.reciplease.repository.mongo;

import org.reciplease.model.PlannedRecipeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlannedRecipeMongoRepository extends MongoRepository<PlannedRecipeDocument, String> {
    List<PlannedRecipeDocument> findByHouseIdAndDateBetween(String houseId, LocalDate start, LocalDate end);

    List<PlannedRecipeDocument> findByHouseIdAndRecipeId(String houseId, String recipeId);
}
