package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.ApiKey;
import org.reciplease.model.HouseRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
@Import(ApiKeyRepositoryImpl.class)
class ApiKeyRepositoryImplTest {

    private static final String HOUSE_ID = "house-1";

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void createPersistsANewApiKey() {
        var apiKey = new ApiKey(
                null,
                HOUSE_ID,
                "Home Assistant",
                HouseRole.READ_ONLY,
                "owner-1",
                "rcpl_abc",
                "hash1",
                Instant.now(),
                null);

        var created = apiKeyRepository.create(apiKey);

        assertThat(created.id(), is(notNullValue()));
        assertThat(apiKeyRepository.findById(created.id()).orElseThrow().name(), is("Home Assistant"));
    }

    @Test
    void findAllForHouseReturnsOnlyThatHousesKeys() {
        apiKeyRepository.create(newKey(HOUSE_ID, "key1", "rcpl_a"));
        apiKeyRepository.create(newKey(HOUSE_ID, "key2", "rcpl_b"));
        apiKeyRepository.create(newKey("house-2", "key3", "rcpl_c"));

        var keys = apiKeyRepository.findAllForHouse(HOUSE_ID);

        assertThat(keys.stream().map(ApiKey::name).toList(), containsInAnyOrder("key1", "key2"));
    }

    @Test
    void findByPrefixFindsTheMatchingKey() {
        apiKeyRepository.create(newKey(HOUSE_ID, "key1", "rcpl_a"));

        var found = apiKeyRepository.findByPrefix("rcpl_a");

        assertThat(found.orElseThrow().name(), is("key1"));
    }

    @Test
    void findByPrefixReturnsEmptyWhenNoKeyMatches() {
        var found = apiKeyRepository.findByPrefix("rcpl_missing");

        assertThat(found.isEmpty(), is(true));
    }

    @Test
    void updateLastUsedAtSetsTheTimestamp() {
        var created = apiKeyRepository.create(newKey(HOUSE_ID, "key1", "rcpl_a"));
        var lastUsedAt = Instant.parse("2026-01-01T00:00:00Z");

        apiKeyRepository.updateLastUsedAt(created.id(), lastUsedAt);

        assertThat(apiKeyRepository.findById(created.id()).orElseThrow().lastUsedAt(), is(lastUsedAt));
    }

    @Test
    void deleteRemovesTheKey() {
        var created = apiKeyRepository.create(newKey(HOUSE_ID, "key1", "rcpl_a"));

        apiKeyRepository.delete(created.id());

        assertThat(apiKeyRepository.findById(created.id()).isEmpty(), is(true));
        assertThat(apiKeyRepository.findAllForHouse(HOUSE_ID), empty());
    }

    private static ApiKey newKey(final String houseId, final String name, final String prefix) {
        return new ApiKey(null, houseId, name, HouseRole.READ_ONLY, "owner-1", prefix, "hash", Instant.now(), null);
    }
}
