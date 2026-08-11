package org.reciplease.repository.mongo;

import org.reciplease.model.ArchivedInventoryItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryArchiveMongoRepository extends MongoRepository<ArchivedInventoryItemDocument, String> {
}
