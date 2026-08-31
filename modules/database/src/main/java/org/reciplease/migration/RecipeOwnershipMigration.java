package org.reciplease.migration;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.reciplease.model.RecipeDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ChangeUnit(id = "recipe-ownership", order = "006", author = "reciplease")
public class RecipeOwnershipMigration {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        final var recipes = mongoTemplate.find(new Query(), RecipeDocument.class);

        recipes.forEach(recipe -> {
            if (recipe.getCreatedBy() != null) {
                mongoTemplate.updateFirst(
                        Query.query(where("_id").is(recipe.getId())),
                        Update.update("ownerId", recipe.getCreatedBy()),
                        RecipeDocument.class);
            } else {
                mongoTemplate.updateFirst(
                        Query.query(where("_id").is(recipe.getId())),
                        Update.update("public", true),
                        RecipeDocument.class);
            }
        });

        mongoTemplate.updateMulti(new Query(), new Update().unset("houseId"), RecipeDocument.class);
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.updateMulti(new Query(), new Update().unset("ownerId"), RecipeDocument.class);
    }
}
