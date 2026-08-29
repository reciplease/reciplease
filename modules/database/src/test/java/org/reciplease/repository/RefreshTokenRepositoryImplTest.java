package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.RefreshTokenRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
@Import(RefreshTokenRepositoryImpl.class)
class RefreshTokenRepositoryImplTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-01-31T00:00:00Z");

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void saveRoundTripsThroughFindByPrefix() {
        var record = newRecord("user-1", "family-1", "prefix000001");

        var saved = refreshTokenRepository.save(record);

        assertThat(saved.id(), is(notNullValue()));
        var found = refreshTokenRepository.findByPrefix("prefix000001");
        assertThat(found.orElseThrow().userId(), is("user-1"));
        assertThat(found.orElseThrow().familyId(), is("family-1"));
        assertThat(found.orElseThrow().tokenHash(), is("hash-1"));
    }

    @Test
    void findByPrefixReturnsEmptyWhenNoRecordMatches() {
        var found = refreshTokenRepository.findByPrefix("missing-prefix");

        assertThat(found.isEmpty(), is(true));
    }

    @Test
    void markUsedSetsTheTimestamp() {
        var saved = refreshTokenRepository.save(newRecord("user-1", "family-1", "prefix000002"));
        var usedAt = Instant.parse("2026-01-02T00:00:00Z");

        refreshTokenRepository.markUsed(saved.id(), usedAt);

        assertThat(
                refreshTokenRepository
                        .findByPrefix("prefix000002")
                        .orElseThrow()
                        .usedAt(),
                is(usedAt));
    }

    @Test
    void revokeFamilyOnlyAffectsThatFamily() {
        refreshTokenRepository.save(newRecord("user-1", "family-1", "prefix000003"));
        refreshTokenRepository.save(newRecord("user-1", "family-2", "prefix000004"));
        var revokedAt = Instant.parse("2026-01-03T00:00:00Z");

        refreshTokenRepository.revokeFamily("family-1", revokedAt);

        assertThat(
                refreshTokenRepository
                        .findByPrefix("prefix000003")
                        .orElseThrow()
                        .revokedAt(),
                is(revokedAt));
        assertThat(
                refreshTokenRepository
                        .findByPrefix("prefix000004")
                        .orElseThrow()
                        .revokedAt(),
                is(nullValue()));
    }

    @Test
    void revokeAllForUserAffectsAllOfThatUsersRecordsAcrossFamilies() {
        refreshTokenRepository.save(newRecord("user-1", "family-1", "prefix000005"));
        refreshTokenRepository.save(newRecord("user-1", "family-2", "prefix000006"));
        refreshTokenRepository.save(newRecord("user-2", "family-3", "prefix000007"));
        var revokedAt = Instant.parse("2026-01-04T00:00:00Z");

        refreshTokenRepository.revokeAllForUser("user-1", revokedAt);

        assertThat(
                refreshTokenRepository
                        .findByPrefix("prefix000005")
                        .orElseThrow()
                        .revokedAt(),
                is(revokedAt));
        assertThat(
                refreshTokenRepository
                        .findByPrefix("prefix000006")
                        .orElseThrow()
                        .revokedAt(),
                is(revokedAt));
        assertThat(
                refreshTokenRepository
                        .findByPrefix("prefix000007")
                        .orElseThrow()
                        .revokedAt(),
                is(nullValue()));
    }

    private static RefreshTokenRecord newRecord(final String userId, final String familyId, final String prefix) {
        return new RefreshTokenRecord(null, userId, familyId, prefix, "hash-1", ISSUED_AT, EXPIRES_AT, null, null);
    }
}
