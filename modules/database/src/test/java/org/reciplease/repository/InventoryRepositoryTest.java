package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.model.IngredientJpa;
import org.reciplease.model.InventoryItemJpa;
import org.reciplease.model.Measure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.Month;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
public class InventoryRepositoryTest {
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @Nested
    class WithSlicesOfBread {

        private LocalDate today;
        private InventoryItemJpa slice_Jan1;
        private InventoryItemJpa slice_Jan2;
        private InventoryItemJpa slice_Jan3;

        @BeforeEach
        void setUp() {
            today = LocalDate.of(2020, Month.JANUARY, 2);

            final IngredientJpa bread = ingredientRepository.save(IngredientJpa.builder()
                    .name("bread")
                    .measure(Measure.ITEMS)
                    .build());

            final var sliceOfBreadBuilder = InventoryItemJpa.builder()
                    .ingredientJpa(bread)
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
            final var expired = inventoryRepository.findByExpirationIsBefore(today);

            assertThat(expired, contains(slice_Jan1));
        }

        @Test
        void shouldGetUnexpiredInventory() {
            final var unexpired = inventoryRepository.findByExpirationIsGreaterThanEqual(today);

            assertThat(unexpired, contains(slice_Jan2, slice_Jan3));
        }
    }
}
