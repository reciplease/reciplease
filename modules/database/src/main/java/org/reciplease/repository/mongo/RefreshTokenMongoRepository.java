package org.reciplease.repository.mongo;

import org.reciplease.model.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenMongoRepository extends MongoRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByTokenPrefix(String tokenPrefix);
}
