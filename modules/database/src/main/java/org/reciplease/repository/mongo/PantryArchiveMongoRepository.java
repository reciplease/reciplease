package org.reciplease.repository.mongo;

import org.reciplease.model.ArchivedPantryItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PantryArchiveMongoRepository extends MongoRepository<ArchivedPantryItemDocument, String> {}
