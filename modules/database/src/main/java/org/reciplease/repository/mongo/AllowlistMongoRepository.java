package org.reciplease.repository.mongo;

import org.reciplease.model.AllowlistDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AllowlistMongoRepository extends MongoRepository<AllowlistDocument, String> {
}
