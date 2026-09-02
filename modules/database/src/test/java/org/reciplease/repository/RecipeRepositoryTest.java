package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
import org.reciplease.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
@Import({RecipeRepositoryImpl.class, MongoAuditingConfig.class})
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

        var updated = recipeRepository.save(
                saved.toBuilder().description("with butter").build());

        assertThat(updated.createdAt(), is(saved.createdAt()));
        assertThat(updated.updatedAt(), is(greaterThanOrEqualTo(saved.updatedAt())));
    }

    @Test
    void shouldFindAllSavedRecipes() {
        var toast = recipeRepository.save(Recipe.builder().name("toast").build());
        var soup = recipeRepository.save(Recipe.builder().name("soup").build());

        assertThat(recipeRepository.findAll(), containsInAnyOrder(toast, soup));
    }

    @Test
    void shouldReturnEmptyListWhenNoRecipesExist() {
        assertThat(recipeRepository.findAll(), is(empty()));
    }

    @Test
    void shouldFindById() {
        var saved = recipeRepository.save(Recipe.builder().name("toast").build());

        assertThat(recipeRepository.findById(saved.id()), is(Optional.of(saved)));
    }

    @Test
    void shouldReturnEmptyWhenIdUnknown() {
        assertThat(recipeRepository.findById("does-not-exist"), is(Optional.empty()));
    }

    @Test
    void shouldDeleteById() {
        var saved = recipeRepository.save(Recipe.builder().name("toast").build());

        recipeRepository.deleteById(saved.id());

        assertThat(recipeRepository.findAll(), is(empty()));
    }

    @Test
    void shouldSeePublicRecipeWithNoVisibleOwners() {
        var publicRecipe = recipeRepository.save(
                Recipe.builder().name("toast").isPublic(true).build());

        assertThat(recipeRepository.findVisibleTo(Set.of()), containsInAnyOrder(publicRecipe));
    }

    @Test
    void shouldSeeOwnRecipeAsTheOwner() {
        var own = recipeRepository.save(Recipe.builder()
                .name("toast")
                .createdBy("viewer")
                .isPublic(false)
                .build());

        assertThat(recipeRepository.findVisibleTo(Set.of("viewer")), containsInAnyOrder(own));
    }

    @Test
    void shouldSeeHousemateRecipeAsAMember() {
        var housemate = recipeRepository.save(Recipe.builder()
                .name("toast")
                .createdBy("alice")
                .isPublic(false)
                .build());

        assertThat(recipeRepository.findVisibleTo(Set.of("alice", "viewer")), containsInAnyOrder(housemate));
    }

    @Test
    void shouldNotSeeAnotherUsersPrivateRecipe() {
        recipeRepository.save(Recipe.builder()
                .name("toast")
                .createdBy("stranger")
                .isPublic(false)
                .build());

        assertThat(recipeRepository.findVisibleTo(Set.of("viewer")), is(empty()));
    }

    @Test
    void shouldSeePublicAndOwnInOneQuery() {
        var own = recipeRepository.save(Recipe.builder()
                .name("toast")
                .createdBy("viewer")
                .isPublic(false)
                .build());
        var publicRecipe = recipeRepository.save(
                Recipe.builder().name("soup").isPublic(true).build());

        assertThat(recipeRepository.findVisibleTo(Set.of("viewer")), containsInAnyOrder(own, publicRecipe));
    }

    @Test
    void shouldFindVisibleByIdForAnOwner() {
        var own = recipeRepository.save(Recipe.builder()
                .name("toast")
                .createdBy("viewer")
                .isPublic(false)
                .build());

        assertThat(recipeRepository.findVisibleById(own.id(), Set.of("viewer")), is(Optional.of(own)));
    }

    @Test
    void shouldNotFindVisibleByIdForAnInvisibleRecipe() {
        var stranger = recipeRepository.save(Recipe.builder()
                .name("toast")
                .createdBy("stranger")
                .isPublic(false)
                .build());

        assertThat(recipeRepository.findVisibleById(stranger.id(), Set.of("viewer")), is(Optional.empty()));
    }
}
