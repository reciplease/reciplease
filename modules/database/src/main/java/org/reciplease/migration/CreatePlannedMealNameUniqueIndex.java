package org.reciplease.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.reciplease.model.PlannedMealDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/** Enforces that a {@link PlannedMealDocument#getName()} is unique per house and date. */
@ChangeUnit(id = "create-planned-meal-name-unique-index", order = "004", author = "reciplease")
public class CreatePlannedMealNameUniqueIndex {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        mongoTemplate
                .indexOps(PlannedMealDocument.class)
                .createIndex(new Index()
                        .on("houseId", Sort.Direction.ASC)
                        .on("date", Sort.Direction.ASC)
                        .on("name", Sort.Direction.ASC)
                        .unique());
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(PlannedMealDocument.class).dropIndex("houseId_1_date_1_name_1");
    }
}
