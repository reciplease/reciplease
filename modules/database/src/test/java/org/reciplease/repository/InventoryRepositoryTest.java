package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataMongoTest
@Import({InventoryRepositoryImpl.class, MongoAuditingConfig.class})
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

            slice_Jan1 = inventoryRepository.save(new InventoryItem(null, null, "bread", "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 1), null));
            slice_Jan2 = inventoryRepository.save(new InventoryItem(null, null, "bread", "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 2), null));
            slice_Jan3 = inventoryRepository.save(new InventoryItem(null, null, "bread", "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 3), null));
        }

        @Test
        void shouldGetExpiredInventory() {
            var expired = inventoryRepository.betweenDates(today);

            assertThat(expired, contains(slice_Jan1));
        }

        @Test
        void shouldGetUnexpiredInventory() {
            var unexpired = inventoryRepository.expiresAfter(today);

            assertThat(unexpired, contains(slice_Jan2, slice_Jan3));
        }
    }

    @Test
    void shouldFindByBarcodeAndPreserveIt() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, "milk", "MILLILITRES", 1000d, LocalDate.of(2026, Month.JUNE, 6), "5012345678900"));

        var found = inventoryRepository.findByBarcode("5012345678900");

        assertThat(found, contains(saved));
        assertThat(found.get(0).barcode(), is("5012345678900"));
    }

    @Test
    void shouldFindByNameIgnoringCase() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, "Bread", "ITEMS", 2d, LocalDate.of(2026, Month.JUNE, 6), null));

        assertThat(inventoryRepository.findByName("bread"), contains(saved));
    }

    @Test
    void shouldReturnEmptyWhenBarcodeUnknown() {
        assertThat(inventoryRepository.findByBarcode("nope"), is(empty()));
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
    }

    @Test
    void shouldPreserveCreatedAtAndAdvanceUpdatedAtOnUpdate() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        var updated = inventoryRepository.save(new InventoryItem(saved.id(), saved.createdBy(), saved.name(), saved.measure(), 12d,
                saved.expiration(), saved.barcode(), saved.createdAt(), saved.updatedAt()));

        assertThat(updated.createdAt(), is(saved.createdAt()));
        assertThat(updated.updatedAt(), is(greaterThanOrEqualTo(saved.updatedAt())));
    }
}
