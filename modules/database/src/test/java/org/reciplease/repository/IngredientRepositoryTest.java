package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@DataMongoTest
@Import(IngredientRepositoryImpl.class)
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void shouldSaveAndFindById() {
        final Ingredient saved = ingredientRepository.save(
                Ingredient.builder().name("Flour").measure("GRAMS").build());

        assertThat(saved.getId(), notNullValue());
        assertThat(ingredientRepository.findById(saved.getId()).orElseThrow(), is(saved));
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertThat(ingredientRepository.findById("unknown").isEmpty(), is(true));
    }

    @Test
    void shouldFindAll() {
        final Ingredient flour = ingredientRepository.save(
                Ingredient.builder().name("Flour").measure("GRAMS").build());
        final Ingredient milk = ingredientRepository.save(
                Ingredient.builder().name("Milk").measure("ML").build());

        assertThat(ingredientRepository.findAll(), containsInAnyOrder(flour, milk));
    }

    @Test
    void shouldSearchByNameCaseInsensitive() {
        final Ingredient flour = ingredientRepository.save(
                Ingredient.builder().name("Flour").measure("GRAMS").build());
        ingredientRepository.save(
                Ingredient.builder().name("Milk").measure("ML").build());

        final List<Ingredient> results = ingredientRepository.searchByName("flour");

        assertThat(results, containsInAnyOrder(flour));
    }

    @Test
    void shouldSearchByPartialName() {
        final Ingredient darkChocolate = ingredientRepository.save(
                Ingredient.builder().name("Dark Chocolate").measure("GRAMS").build());
        final Ingredient milkChocolate = ingredientRepository.save(
                Ingredient.builder().name("Milk Chocolate").measure("GRAMS").build());
        ingredientRepository.save(
                Ingredient.builder().name("Milk").measure("ML").build());

        final List<Ingredient> results = ingredientRepository.searchByName("chocolate");

        assertThat(results, containsInAnyOrder(darkChocolate, milkChocolate));
    }

    @Test
    void shouldReturnEmptyListForNoMatch() {
        ingredientRepository.save(
                Ingredient.builder().name("Flour").measure("GRAMS").build());

        assertThat(ingredientRepository.searchByName("xyz"), is(empty()));
    }

    @Test
    void shouldDeleteById() {
        final Ingredient saved = ingredientRepository.save(
                Ingredient.builder().name("Flour").measure("GRAMS").build());

        ingredientRepository.deleteById(saved.getId());

        assertThat(ingredientRepository.findById(saved.getId()).isEmpty(), is(true));
    }

    @Test
    void shouldSaveAll() {
        final List<Ingredient> saved = ingredientRepository.saveAll(List.of(
                Ingredient.builder().name("Flour").measure("GRAMS").build(),
                Ingredient.builder().name("Milk").measure("ML").build()
        ));

        assertThat(saved, not(empty()));
        saved.forEach(i -> assertThat(i.getId(), notNullValue()));
    }
}
