package org.reciplease.repository.mongo;

import org.reciplease.model.FitbitConnectionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FitbitConnectionMongoRepository extends MongoRepository<FitbitConnectionDocument, String> {
}
