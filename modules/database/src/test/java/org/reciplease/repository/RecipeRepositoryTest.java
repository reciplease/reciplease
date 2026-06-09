package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataMongoTest
@Import(RecipeRepositoryImpl.class)
class RecipeRepositoryTest {
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        var saved = recipeRepository.save(Recipe.builder().name("toast").build());

        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
    }

    @Test
    void shouldPreserveCreatedAtAndAdvanceUpdatedAtOnUpdate() {
        var saved = recipeRepository.save(Recipe.builder().name("toast").build());

        var updated = recipeRepository.save(saved.toBuilder().description("with butter").build());

        assertThat(updated.createdAt(), is(saved.createdAt()));
        assertThat(updated.updatedAt(), is(greaterThanOrEqualTo(saved.updatedAt())));
    }
}
