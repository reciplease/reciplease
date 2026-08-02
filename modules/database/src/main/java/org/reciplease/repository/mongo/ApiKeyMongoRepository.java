package org.reciplease.repository.mongo;

import org.reciplease.model.ApiKeyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyMongoRepository extends MongoRepository<ApiKeyDocument, String> {
    List<ApiKeyDocument> findAllByHouseId(String houseId);

    Optional<ApiKeyDocument> findByKeyPrefix(String keyPrefix);
}
