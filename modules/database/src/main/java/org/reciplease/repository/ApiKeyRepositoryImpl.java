package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.ApiKey;
import org.reciplease.model.ApiKeyDocument;
import org.reciplease.repository.mongo.ApiKeyMongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ApiKeyRepositoryImpl implements ApiKeyRepository {

    private final ApiKeyMongoRepository apiKeyMongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public ApiKey create(final ApiKey apiKey) {
        return apiKeyMongoRepository.save(ApiKeyDocument.from(apiKey)).toModel();
    }

    @Override
    public List<ApiKey> findAllForHouse(final String houseId) {
        return apiKeyMongoRepository.findAllByHouseId(houseId).stream()
                .map(ApiKeyDocument::toModel)
                .collect(toList());
    }

    @Override
    public Optional<ApiKey> findByPrefix(final String keyPrefix) {
        return apiKeyMongoRepository.findByKeyPrefix(keyPrefix).map(ApiKeyDocument::toModel);
    }

    @Override
    public void updateLastUsedAt(final String id, final Instant lastUsedAt) {
        mongoTemplate.updateFirst(
                query(where("_id").is(id)), new Update().set("lastUsedAt", lastUsedAt), ApiKeyDocument.class);
    }

    @Override
    public Optional<ApiKey> findById(final String id) {
        return apiKeyMongoRepository.findById(id).map(ApiKeyDocument::toModel);
    }

    @Override
    public void delete(final String id) {
        apiKeyMongoRepository.deleteById(id);
    }
}
