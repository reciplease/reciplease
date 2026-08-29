package org.reciplease.repository.mongo;

import org.reciplease.model.RecipeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecipeMongoRepository extends MongoRepository<RecipeDocument, String> {}
