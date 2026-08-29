package org.reciplease.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.reciplease.model.ApiKey;

public interface ApiKeyRepository {

    /** Persists a brand-new API key. */
    ApiKey create(ApiKey apiKey);

    List<ApiKey> findAllForHouse(String houseId);

    Optional<ApiKey> findByPrefix(String keyPrefix);

    void updateLastUsedAt(String id, Instant lastUsedAt);

    Optional<ApiKey> findById(String id);

    void delete(String id);
}
