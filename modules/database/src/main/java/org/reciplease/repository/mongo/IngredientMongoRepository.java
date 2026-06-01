package org.reciplease.repository.mongo;

import org.reciplease.model.IngredientDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IngredientMongoRepository extends MongoRepository<IngredientDocument, String> {
    List<IngredientDocument> findByNameContainingIgnoreCase(String name);
}
