package org.reciplease.repository.mongo;

import org.reciplease.model.HouseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HouseMongoRepository extends MongoRepository<HouseDocument, String> {}
