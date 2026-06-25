package org.reciplease.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.reciplease.model.UserDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/** Backs {@link UserDocument#getHandle()}'s {@code @Indexed(unique = true, sparse = true)}. */
@ChangeUnit(id = "create-user-handle-unique-index", order = "001", author = "reciplease")
public class CreateUserHandleUniqueIndex {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(UserDocument.class)
                .createIndex(new Index().on("handle", Sort.Direction.ASC).unique().sparse());
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(UserDocument.class).dropIndex("handle");
    }
}
