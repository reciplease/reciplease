package org.reciplease.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.reciplease.model.PendingPantryItemDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;

@ChangeUnit(id = "create-pending-pantry-scan-id-unique-index", order = "007", author = "reciplease")
public class CreatePendingPantryScanIdUniqueIndex {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        mongoTemplate
                .indexOps(PendingPantryItemDocument.class)
                .createIndex(new Index()
                        .on("houseId", Sort.Direction.ASC)
                        .on("scanId", Sort.Direction.ASC)
                        .unique()
                        .partial(PartialIndexFilter.of(Criteria.where("scanId").exists(true))));
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(PendingPantryItemDocument.class).dropIndex("houseId_1_scanId_1");
    }
}
