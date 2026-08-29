package org.reciplease.repository.mongo;

import java.util.List;
import java.util.Optional;
import org.reciplease.model.ApiKeyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiKeyMongoRepository extends MongoRepository<ApiKeyDocument, String> {
    List<ApiKeyDocument> findAllByHouseId(String houseId);

    Optional<ApiKeyDocument> findByKeyPrefix(String keyPrefix);
}
