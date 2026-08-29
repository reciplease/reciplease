package org.reciplease.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.reciplease.model.InviteDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/** Backs {@link InviteDocument#getCode()}'s {@code @Indexed(unique = true)}. */
@ChangeUnit(id = "create-invite-code-unique-index", order = "002", author = "reciplease")
public class CreateInviteCodeUniqueIndex {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        mongoTemplate
                .indexOps(InviteDocument.class)
                .createIndex(new Index().on("code", Sort.Direction.ASC).unique());
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(InviteDocument.class).dropIndex("code");
    }
}
