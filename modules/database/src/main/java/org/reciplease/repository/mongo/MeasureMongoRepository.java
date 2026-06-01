package org.reciplease.repository.mongo;

import org.reciplease.model.MeasureDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MeasureMongoRepository extends MongoRepository<MeasureDocument, String> {
}
