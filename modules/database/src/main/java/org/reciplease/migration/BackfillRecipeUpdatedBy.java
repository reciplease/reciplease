package org.reciplease.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.reciplease.model.RecipeDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Recipes saved before {@link RecipeDocument#getUpdatedBy()} existed have no
 * {@code updatedBy}. Backfills it from the recipe's {@code createdBy} so existing recipes
 * have a "last updated by" to display instead of nothing.
 */
@ChangeUnit(id = "backfill-recipe-updated-by", order = "005", author = "reciplease")
public class BackfillRecipeUpdatedBy {

    @Execution
    public void execution(final MongoTemplate mongoTemplate) {
        final var recipesMissingUpdatedBy = mongoTemplate.find(
                Query.query(where("updatedBy").is(null)), RecipeDocument.class);

        recipesMissingUpdatedBy.forEach(recipe -> mongoTemplate.updateFirst(
                Query.query(where("_id").is(recipe.getId())),
                Update.update("updatedBy", recipe.getCreatedBy()),
                RecipeDocument.class));
    }

    @RollbackExecution
    public void rollback(final MongoTemplate mongoTemplate) {
        mongoTemplate.updateMulti(new Query(), Update.update("updatedBy", null), RecipeDocument.class);
    }
}
