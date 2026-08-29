package org.reciplease.repository.mongo;

import java.util.Optional;
import org.reciplease.model.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenMongoRepository extends MongoRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByTokenPrefix(String tokenPrefix);
}
