package org.reciplease.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Duration;
import org.reciplease.model.WebAuthnChallengeDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/** Backs {@link WebAuthnChallengeDocument#getCreatedAt()}'s {@code @Indexed(expireAfter = "5m")}. */
@ChangeUnit(id = "create-webauthn-challenge-ttl-index", order = "003", author = "reciplease")
public class CreateWebAuthnChallengeTtlIndex {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        mongoTemplate
                .indexOps(WebAuthnChallengeDocument.class)
                .createIndex(new Index().on("createdAt", Sort.Direction.ASC).expire(Duration.ofMinutes(5)));
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(WebAuthnChallengeDocument.class).dropIndex("createdAt");
    }
}
