package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
import org.reciplease.model.PantryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataMongoTest
@Import({PantryRepositoryImpl.class, MongoAuditingConfig.class})
class PantryRepositoryTest {
    private static final String HOUSE_ID = "house-1";

    @Autowired
    private PantryRepository pantryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private org.reciplease.repository.mongo.PantryArchiveMongoRepository pantryArchiveMongoRepository;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Nested
    class WithSlicesOfBread {

        private LocalDate today;
        private PantryItem slice_Jan1;
        private PantryItem slice_Jan2;
        private PantryItem slice_Jan3;

        @BeforeEach
        void setUp() {
            today = LocalDate.of(2020, Month.JANUARY, 2);

            slice_Jan1 = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 1), null));
            slice_Jan2 = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 2), null));
            slice_Jan3 = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 1d, LocalDate.of(2020, Month.JANUARY, 3), null));
        }

        @Test
        void shouldGetExpiredPantry() {
            var expired = pantryRepository.betweenDates(HOUSE_ID, today);

            assertThat(expired, contains(slice_Jan1));
        }

        @Test
        void shouldGetUnexpiredPantry() {
            var unexpired = pantryRepository.expiresAfter(HOUSE_ID, today);

            assertThat(unexpired, contains(slice_Jan2, slice_Jan3));
        }
    }

    @Test
    @DisplayName("expiresAfter orders results alphabetically by name")
    void shouldOrderUnexpiredAlphabeticallyByName() {
        var today = LocalDate.of(2026, Month.JUNE, 1);
        var milk = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "milk", null, "MILLILITRES", 500d, LocalDate.of(2026, Month.JUNE, 10), null));
        var eggs = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 10), null));
        var bread = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 1d, LocalDate.of(2026, Month.JUNE, 10), null));

        var unexpired = pantryRepository.expiresAfter(HOUSE_ID, today);

        assertThat(unexpired, contains(bread, eggs, milk));
    }

    @Test
    @DisplayName("betweenDates orders results alphabetically by name")
    void shouldOrderExpiredAlphabeticallyByName() {
        var today = LocalDate.of(2026, Month.JUNE, 10);
        var milk = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "milk", null, "MILLILITRES", 500d, LocalDate.of(2026, Month.JUNE, 1), null));
        var eggs = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 1), null));
        var bread = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 1d, LocalDate.of(2026, Month.JUNE, 1), null));

        var expired = pantryRepository.betweenDates(HOUSE_ID, today);

        assertThat(expired, contains(bread, eggs, milk));
    }

    @Test
    void shouldFindByBarcodeAndPreserveIt() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "milk", null, "MILLILITRES", 1000d, LocalDate.of(2026, Month.JUNE, 6), "5012345678900"));

        var found = pantryRepository.findByBarcode(HOUSE_ID, "5012345678900");

        assertThat(found, contains(saved));
        assertThat(found.getFirst().barcode(), is("5012345678900"));
    }

    @Test
    void shouldFindByBarcodeIn() {
        var milk = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "milk", null, "MILLILITRES", 1000d, LocalDate.of(2026, Month.JUNE, 6), "5012345678900"));
        var eggs = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 6), "5012345678901"));
        pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 1d, LocalDate.of(2026, Month.JUNE, 6), "5012345678902"));

        var found = pantryRepository.findByBarcodeIn(HOUSE_ID, Set.of("5012345678900", "5012345678901"));

        assertThat(found, containsInAnyOrder(milk, eggs));
    }

    @Test
    void shouldPersistAndRetrieveBrand() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "tomato ketchup", "Heinz", "MILLILITRES", 500d, LocalDate.of(2026, Month.JUNE, 6), null));

        var found = pantryRepository.findById(saved.id());

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().brand(), is("Heinz"));
    }

    @Test
    void shouldPersistAndRetrieveImageBytes() {
        var imageBytes = new byte[]{1, 2, 3, 4};
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "milk", null, "MILLILITRES", 1000d, LocalDate.of(2026, Month.JUNE, 6), null, imageBytes));

        var found = pantryRepository.findById(saved.id());

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().image(), is(imageBytes));
    }

    @Test
    void shouldFindByNameIgnoringCase() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "Bread", null, "ITEMS", 2d, LocalDate.of(2026, Month.JUNE, 6), null));

        assertThat(pantryRepository.findByName(HOUSE_ID, "bread"), contains(saved));
    }

    @Test
    void shouldReturnEmptyWhenBarcodeUnknown() {
        assertThat(pantryRepository.findByBarcode(HOUSE_ID, "nope"), is(empty()));
    }

    @Test
    void shouldFindById() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        assertThat(pantryRepository.findById(saved.id()), is(Optional.of(saved)));
    }

    @Test
    void shouldReturnEmptyWhenIdUnknown() {
        assertThat(pantryRepository.findById("does-not-exist"), is(Optional.empty()));
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
    }

    @Test
    void shouldDeleteById() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        pantryRepository.deleteById(saved.id());

        assertThat(pantryRepository.findById(saved.id()), is(Optional.empty()));
    }

    @Test
    @DisplayName("deleteById archives a snapshot of the item before removing it")
    void shouldArchiveOnDelete() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, 2d, LocalDate.of(2026, Month.JUNE, 20), "5012345678900"));

        pantryRepository.deleteById(saved.id());

        var archived = pantryArchiveMongoRepository.findAll();
        assertThat(archived, hasSize(1));
        assertThat(archived.getFirst().getOriginalId(), is(saved.id()));
        assertThat(archived.getFirst().getHouseId(), is(HOUSE_ID));
        assertThat(archived.getFirst().getName(), is("eggs"));
        assertThat(archived.getFirst().getRemaining(), is(2d));
        assertThat(archived.getFirst().getBarcode(), is("5012345678900"));
        assertThat(archived.getFirst().getArchivedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("deleteById on an already-missing item is a no-op, not an error")
    void shouldNotArchiveWhenDeletingUnknownItem() {
        pantryRepository.deleteById("does-not-exist");

        assertThat(pantryArchiveMongoRepository.findAll(), is(empty()));
    }

    @Test
    void shouldFindAllZeroRemaining() {
        var zero = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, 0d, LocalDate.of(2026, Month.JUNE, 20), null));
        pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "milk", null, "MILLILITRES", 500d, 100d, LocalDate.of(2026, Month.JUNE, 20), null));

        assertThat(pantryRepository.findAllZeroRemaining(), contains(zero));
    }

    @Test
    void shouldFindAllById() {
        var eggs = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));
        var milk = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "milk", null, "MILLILITRES", 500d, LocalDate.of(2026, Month.JUNE, 20), null));
        pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 1d, LocalDate.of(2026, Month.JUNE, 20), null));

        var found = pantryRepository.findAllById(List.of(eggs.id(), milk.id()));

        assertThat(found, containsInAnyOrder(eggs, milk));
    }

    @Test
    void shouldPreserveCreatedAtAndAdvanceUpdatedAtOnUpdate() {
        var saved = pantryRepository.save(new PantryItem(null, null, HOUSE_ID, "eggs", null, "ITEMS", 6d, LocalDate.of(2026, Month.JUNE, 20), null));

        var updated = pantryRepository.save(new PantryItem(saved.id(), saved.createdBy(), saved.houseId(), saved.name(), null, saved.measure(), 12d,
                saved.remaining(), saved.expiration(), saved.barcode(), saved.image(), saved.createdAt(), saved.updatedAt()));

        assertThat(updated.createdAt(), is(saved.createdAt()));
        assertThat(updated.updatedAt(), is(greaterThanOrEqualTo(saved.updatedAt())));
    }
}
