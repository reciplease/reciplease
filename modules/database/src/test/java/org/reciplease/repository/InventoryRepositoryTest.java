package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.time.Month;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@DataMongoTest
@Import({IngredientRepositoryImpl.class, InventoryRepositoryImpl.class})
class InventoryRepositoryTest {
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Nested
    class WithSlicesOfBread {

        private LocalDate today;
        private InventoryItem slice_Jan1;
        private InventoryItem slice_Jan2;
        private InventoryItem slice_Jan3;

        @BeforeEach
        void setUp() {
            today = LocalDate.of(2020, Month.JANUARY, 2);

            final Ingredient bread = ingredientRepository.save(Ingredient.builder()
                    .name("bread")
                    .measure("ITEMS")
                    .build());

            final var sliceOfBreadBuilder = InventoryItem.builder()
                    .ingredient(bread)
                    .amount(1d);

            slice_Jan1 = inventoryRepository.save(sliceOfBreadBuilder
                    .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                    .build());
            slice_Jan2 = inventoryRepository.save(sliceOfBreadBuilder
                    .expiration(LocalDate.of(2020, Month.JANUARY, 2))
                    .build());
            slice_Jan3 = inventoryRepository.save(sliceOfBreadBuilder
                    .expiration(LocalDate.of(2020, Month.JANUARY, 3))
                    .build());
        }

        @Test
        void shouldGetExpiredInventory() {
            final var expired = inventoryRepository.betweenDates(today);

            assertThat(expired, contains(slice_Jan1));
        }

        @Test
        void shouldGetUnexpiredInventory() {
            final var unexpired = inventoryRepository.expiresAfter(today);

            assertThat(unexpired, contains(slice_Jan2, slice_Jan3));
        }
    }
}
