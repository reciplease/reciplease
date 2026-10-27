package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
import org.reciplease.migration.CreatePendingPantryScanIdUniqueIndex;
import org.reciplease.model.PendingPantryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
@Import({PendingPantryRepositoryImpl.class, MongoAuditingConfig.class})
class PendingPantryRepositoryTest {
    private static final String HOUSE_ID = "house-1";

    @Autowired
    private PendingPantryRepository pendingPantryRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void shouldSaveAndFindByIdIncludingImages() {
        var barcodeImage = new byte[] {7, 8, 9};
        var expirationImage = new byte[] {1, 2, 3};
        var measureImage = new byte[] {4, 5, 6};
        var saved = pendingPantryRepository.save(
                new PendingPantryItem(null, null, HOUSE_ID, barcodeImage, expirationImage, measureImage));

        var found = pendingPantryRepository.findById(saved.id());

        assertThat(found, is(Optional.of(saved)));
        assertThat(found.get().barcodeImage(), is(barcodeImage));
        assertThat(found.get().expirationImage(), is(expirationImage));
        assertThat(found.get().measureImage(), is(measureImage));
    }

    @Test
    @DisplayName("reads a pre-migration document's \"barcode\" string field as legacyBarcode, not barcodeImage")
    void shouldReadLegacyBarcodeFromPreMigrationDocument() {
        mongoTemplate
                .getCollection("pendingPantry")
                .insertOne(new Document().append("houseId", HOUSE_ID).append("barcode", "5012345678900"));

        var found = pendingPantryRepository.findAllByHouseId(HOUSE_ID);

        assertThat(found.get(0).legacyBarcode(), is("5012345678900"));
        assertThat(found.get(0).barcodeImage(), is(nullValue()));
    }

    @Test
    void shouldSaveItemWithAllOptionalFieldsMissing() {
        var saved = pendingPantryRepository.save(new PendingPantryItem(null, null, HOUSE_ID, null, null, null));

        assertThat(pendingPantryRepository.findById(saved.id()), is(Optional.of(saved)));
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        var saved = pendingPantryRepository.save(new PendingPantryItem(null, null, HOUSE_ID, null, null, null));

        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("findAllByHouseId returns only that house's items, oldest first")
    void shouldFindAllByHouseIdOldestFirst() {
        var first =
                pendingPantryRepository.save(new PendingPantryItem(null, null, HOUSE_ID, new byte[] {1}, null, null));
        var second =
                pendingPantryRepository.save(new PendingPantryItem(null, null, HOUSE_ID, new byte[] {2}, null, null));
        pendingPantryRepository.save(new PendingPantryItem(null, null, "other-house", new byte[] {3}, null, null));

        var found = pendingPantryRepository.findAllByHouseId(HOUSE_ID);

        assertThat(found, contains(first, second));
    }

    @Test
    @DisplayName("saving the same scanId twice for a house returns the original item instead of duplicating it")
    void shouldBeIdempotentPerHouseAndScanId() {
        new CreatePendingPantryScanIdUniqueIndex().execution(mongoTemplate);
        var first = pendingPantryRepository.save(scanned(HOUSE_ID, "scan-1", new byte[] {1}));

        var second = pendingPantryRepository.save(scanned(HOUSE_ID, "scan-1", new byte[] {2}));

        assertThat(second, is(first));
        assertThat(pendingPantryRepository.findAllByHouseId(HOUSE_ID), contains(first));
    }

    @Test
    @DisplayName("the same scanId in a different house is a different item")
    void shouldScopeScanIdToHouse() {
        new CreatePendingPantryScanIdUniqueIndex().execution(mongoTemplate);
        var mine = pendingPantryRepository.save(scanned(HOUSE_ID, "scan-1", new byte[] {1}));

        var theirs = pendingPantryRepository.save(scanned("other-house", "scan-1", new byte[] {2}));

        assertThat(theirs.id().equals(mine.id()), is(false));
    }

    @Test
    @DisplayName("items without a scanId never collide with each other")
    void shouldAllowManyItemsWithoutScanId() {
        new CreatePendingPantryScanIdUniqueIndex().execution(mongoTemplate);

        pendingPantryRepository.save(new PendingPantryItem(null, null, HOUSE_ID, new byte[] {1}, null, null));
        pendingPantryRepository.save(new PendingPantryItem(null, null, HOUSE_ID, new byte[] {2}, null, null));

        assertThat(pendingPantryRepository.findAllByHouseId(HOUSE_ID).size(), is(2));
    }

    @Test
    @DisplayName("the unique index returns the winner when a concurrent save slips past the lookup")
    void shouldFallBackToExistingItemOnDuplicateKey() {
        new CreatePendingPantryScanIdUniqueIndex().execution(mongoTemplate);
        var winner = pendingPantryRepository.save(scanned(HOUSE_ID, "scan-1", new byte[] {1}));
        var loser = scanned(HOUSE_ID, "scan-1", new byte[] {2});

        var raced = ((PendingPantryRepositoryImpl) pendingPantryRepository).insertOrFindExisting(loser);

        assertThat(raced, is(winner));
    }

    private static PendingPantryItem scanned(final String houseId, final String scanId, final byte[] barcodeImage) {
        return new PendingPantryItem(null, null, houseId, barcodeImage, null, null, null, null, null, scanId);
    }

    @Test
    void shouldReturnEmptyWhenIdUnknown() {
        assertThat(pendingPantryRepository.findById("does-not-exist"), is(Optional.empty()));
    }

    @Test
    void shouldDeleteById() {
        var saved = pendingPantryRepository.save(new PendingPantryItem(null, null, HOUSE_ID, null, null, null));

        pendingPantryRepository.deleteById(saved.id());

        assertThat(pendingPantryRepository.findById(saved.id()), is(Optional.empty()));
    }
}
