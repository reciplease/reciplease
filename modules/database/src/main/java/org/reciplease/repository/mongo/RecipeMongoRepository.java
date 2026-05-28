package org.reciplease.repository.mongo;

import org.reciplease.model.RecipeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface RecipeMongoRepository extends MongoRepository<RecipeDocument, UUID> {
}
