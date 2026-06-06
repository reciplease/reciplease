package org.reciplease.repository.mongo;

import org.reciplease.model.PlannedRecipeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlannedRecipeMongoRepository extends MongoRepository<PlannedRecipeDocument, String> {
    List<PlannedRecipeDocument> findByDateBetween(LocalDate start, LocalDate end);

    List<PlannedRecipeDocument> findByRecipeId(String recipeId);
}
