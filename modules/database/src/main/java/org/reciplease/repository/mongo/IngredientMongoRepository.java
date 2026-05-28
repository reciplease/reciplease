package org.reciplease.repository.mongo;

import org.reciplease.model.IngredientDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface IngredientMongoRepository extends MongoRepository<IngredientDocument, UUID> {
    List<IngredientDocument> findByNameContainingIgnoreCase(String name);
}
