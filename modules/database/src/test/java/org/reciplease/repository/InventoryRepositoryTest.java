package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataMongoTest
@Import({InventoryRepositoryImpl.class, MongoAuditingConfig.class})
class InventoryRepositoryTest {
    private static final String HOUSE_ID = "house-1";

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

            slice_Jan1 = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 1), null));
            slice_Jan2 = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 2), null));
            slice_Jan3 = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 3), null));
        }

        @Test
        void shouldGetExpiredInventory() {
            var expired = inventoryRepository.betweenDates(HOUSE_ID, today);

            assertThat(expired, contains(slice_Jan1));
        }

        @Test
        void shouldGetUnexpiredInventory() {
            var unexpired = inventoryRepository.expiresAfter(HOUSE_ID, today);

            assertThat(unexpired, contains(slice_Jan2, slice_Jan3));
        }
    }

    @Test
    @DisplayName("expiresAfter orders results alphabetically by name")
    void shouldOrderUnexpiredAlphabeticallyByName() {
        var today = LocalDate.of(2026, Month.JUNE, 1);
        var milk = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "milk", "MILLILITRES", 500d, LocalDate.of(2026, Month.JUNE, 10), null));
        var eggs = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 10), null));
        var bread = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 1d, LocalDate.of(2026, Month.JUNE, 10), null));

        var unexpired = inventoryRepository.expiresAfter(HOUSE_ID, today);

        assertThat(unexpired, contains(bread, eggs, milk));
    }

    @Test
    @DisplayName("betweenDates orders results alphabetically by name")
    void shouldOrderExpiredAlphabeticallyByName() {
        var today = LocalDate.of(2026, Month.JUNE, 10);
        var milk = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "milk", "MILLILITRES", 500d, LocalDate.of(2026, Month.JUNE, 1), null));
        var eggs = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 1), null));
        var bread = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 1d, LocalDate.of(2026, Month.JUNE, 1), null));

        var expired = inventoryRepository.betweenDates(HOUSE_ID, today);

        assertThat(expired, contains(bread, eggs, milk));
    }

    @Test
    void shouldFindByBarcodeAndPreserveIt() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "milk", "MILLILITRES", 1000d, LocalDate.of(2026, Month.JUNE, 6), "5012345678900"));

        var found = inventoryRepository.findByBarcode(HOUSE_ID, "5012345678900");

        assertThat(found, contains(saved));
        assertThat(found.get(0).barcode(), is("5012345678900"));
    }

    @Test
    void shouldFindByBarcodeIn() {
        var milk = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "milk", "MILLILITRES", 1000d, LocalDate.of(2026, Month.JUNE, 6), "5012345678900"));
        var eggs = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 6), "5012345678901"));
        inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 1d, LocalDate.of(2026, Month.JUNE, 6), "5012345678902"));

        var found = inventoryRepository.findByBarcodeIn(HOUSE_ID, Set.of("5012345678900", "5012345678901"));

        assertThat(found, containsInAnyOrder(milk, eggs));
    }

    @Test
    void shouldPersistAndRetrieveImageBytes() {
        var imageBytes = new byte[]{1, 2, 3, 4};
        var saved = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "milk", "MILLILITRES", 1000d, LocalDate.of(2026, Month.JUNE, 6), null, imageBytes));

        var found = inventoryRepository.findById(saved.id());

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().image(), is(imageBytes));
    }

    @Test
    void shouldFindByNameIgnoringCase() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "Bread", "ITEMS", 2d, LocalDate.of(2026, Month.JUNE, 6), null));

        assertThat(inventoryRepository.findByName(HOUSE_ID, "bread"), contains(saved));
    }

    @Test
    void shouldReturnEmptyWhenBarcodeUnknown() {
        assertThat(inventoryRepository.findByBarcode(HOUSE_ID, "nope"), is(empty()));
    }

    @Test
    void shouldFindById() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        assertThat(inventoryRepository.findById(saved.id()), is(Optional.of(saved)));
    }

    @Test
    void shouldReturnEmptyWhenIdUnknown() {
        assertThat(inventoryRepository.findById("does-not-exist"), is(Optional.empty()));
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
    }

    @Test
    void shouldDeleteById() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        inventoryRepository.deleteById(saved.id());

        assertThat(inventoryRepository.findById(saved.id()), is(Optional.empty()));
    }

    @Test
    void shouldPreserveCreatedAtAndAdvanceUpdatedAtOnUpdate() {
        var saved = inventoryRepository.save(new InventoryItem(null, null, HOUSE_ID, "eggs", "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        var updated = inventoryRepository.save(new InventoryItem(saved.id(), saved.createdBy(), saved.houseId(), saved.name(), saved.measure(), 12d,
                saved.remaining(), saved.expiration(), saved.barcode(), saved.image(), saved.createdAt(), saved.updatedAt()));

        assertThat(updated.createdAt(), is(saved.createdAt()));
        assertThat(updated.updatedAt(), is(greaterThanOrEqualTo(saved.updatedAt())));
    }
}
