package org.reciplease.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Duration;
import org.reciplease.model.RefreshTokenDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * Refresh tokens expire at a variable time per-record (30 days from issuance), unlike
 * {@link org.reciplease.model.WebAuthnChallengeDocument}'s fixed 5-minute TTL, so this indexes
 * {@link RefreshTokenDocument#getExpiresAt()} in Mongo's "expire at the time stored in the
 * field" TTL mode ({@code expire(Duration.ZERO)}) rather than a fixed offset from creation.
 */
@ChangeUnit(id = "create-refresh-token-ttl-index", order = "006", author = "reciplease")
public class CreateRefreshTokenTtlIndex {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        mongoTemplate
                .indexOps(RefreshTokenDocument.class)
                .createIndex(new Index().on("expiresAt", Sort.Direction.ASC).expire(Duration.ZERO));
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(RefreshTokenDocument.class).dropIndex("expiresAt");
    }
}
