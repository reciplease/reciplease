package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.model.InventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.time.Month;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@DataMongoTest
@Import(InventoryRepositoryImpl.class)
class InventoryRepositoryTest {
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

            final var sliceOfBreadBuilder = InventoryItem.builder()
                    .name("bread")
                    .measure("ITEMS")
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

    @Test
    void shouldFindByBarcodeAndPreserveIt() {
        final var saved = inventoryRepository.save(InventoryItem.builder()
                .name("milk").measure("MILLILITRES").amount(1000d)
                .expiration(LocalDate.of(2026, Month.JUNE, 6))
                .barcode("5012345678900")
                .build());

        final var found = inventoryRepository.findByBarcode("5012345678900");

        assertThat(found, contains(saved));
        assertThat(found.get(0).getBarcode(), is("5012345678900"));
    }

    @Test
    void shouldFindByNameIgnoringCase() {
        final var saved = inventoryRepository.save(InventoryItem.builder()
                .name("Bread").measure("ITEMS").amount(2d)
                .expiration(LocalDate.of(2026, Month.JUNE, 6))
                .build());

        assertThat(inventoryRepository.findByName("bread"), contains(saved));
    }

    @Test
    void shouldReturnEmptyWhenBarcodeUnknown() {
        assertThat(inventoryRepository.findByBarcode("nope"), is(empty()));
    }
}
