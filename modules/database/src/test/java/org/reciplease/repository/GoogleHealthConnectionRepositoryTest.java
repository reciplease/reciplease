package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.GoogleHealthConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataMongoTest
@Import(GoogleHealthConnectionRepositoryImpl.class)
class GoogleHealthConnectionRepositoryTest {

    private static final String USER_ID = "user-1";

    @Autowired
    private GoogleHealthConnectionRepository googleHealthConnectionRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    @DisplayName("save persists a new connection keyed by userId")
    void savePersistsConnection() {
        final var createdAt = Instant.parse("2026-07-02T12:00:00Z");
        final var connection = new GoogleHealthConnection(USER_ID, "access-1", "refresh-1",
                Instant.parse("2026-07-02T20:00:00Z"), "nutrition.readonly", createdAt, createdAt);

        final var saved = googleHealthConnectionRepository.save(connection);

        assertThat(saved.userId(), is(USER_ID));
        assertThat(saved.accessToken(), is("access-1"));
        assertThat(saved.refreshToken(), is("refresh-1"));
        assertThat(saved.scope(), is("nutrition.readonly"));
        assertThat(saved.createdAt(), is(createdAt));
        assertThat(saved.updatedAt(), is(createdAt));
    }

    @Test
    @DisplayName("save overwrites the existing connection for the same userId (upsert, e.g. on token refresh)")
    void saveOverwritesExistingConnectionForSameUser() {
        final var original = googleHealthConnectionRepository.save(
                new GoogleHealthConnection(USER_ID, "access-1", "refresh-1", Instant.parse("2026-07-02T12:00:00Z"), "nutrition.readonly", null, null));

        final var refreshed = googleHealthConnectionRepository.save(
                new GoogleHealthConnection(USER_ID, "access-2", "refresh-2", Instant.parse("2026-07-02T13:00:00Z"), "nutrition.readonly",
                        original.createdAt(), original.updatedAt()));

        final var found = googleHealthConnectionRepository.findByUserId(USER_ID);
        assertThat(found.isPresent(), is(true));
        assertThat(found.get().accessToken(), is("access-2"));
        assertThat(found.get().refreshToken(), is("refresh-2"));
        assertThat(refreshed.createdAt(), is(original.createdAt()));
    }

    @Test
    @DisplayName("findByUserId returns empty when no connection is linked")
    void findByUserIdReturnsEmptyWhenMissing() {
        assertThat(googleHealthConnectionRepository.findByUserId("unknown-user").isPresent(), is(false));
    }

    @Test
    @DisplayName("deleteByUserId removes the connection")
    void deleteByUserIdRemovesConnection() {
        googleHealthConnectionRepository.save(
                new GoogleHealthConnection(USER_ID, "access-1", "refresh-1", Instant.parse("2026-07-02T12:00:00Z"), "nutrition.readonly", null, null));

        googleHealthConnectionRepository.deleteByUserId(USER_ID);

        assertThat(googleHealthConnectionRepository.findByUserId(USER_ID).isPresent(), is(false));
    }

    @Test
    @DisplayName("deleteByUserId is a no-op when there is no connection")
    void deleteByUserIdIsNoOpWhenMissing() {
        googleHealthConnectionRepository.deleteByUserId("unknown-user");
    }
}
