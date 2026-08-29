package org.reciplease.repository.mongo;

import org.reciplease.model.GoogleHealthConnectionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoogleHealthConnectionMongoRepository
        extends MongoRepository<GoogleHealthConnectionDocument, String> {}
