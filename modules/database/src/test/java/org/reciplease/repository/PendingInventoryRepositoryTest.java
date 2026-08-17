package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
import org.bson.Document;
import org.reciplease.model.PendingInventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DataMongoTest
@Import({PendingInventoryRepositoryImpl.class, MongoAuditingConfig.class})
class PendingInventoryRepositoryTest {
    private static final String HOUSE_ID = "house-1";

    @Autowired
    private PendingInventoryRepository pendingInventoryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void shouldSaveAndFindByIdIncludingImages() {
        var barcodeImage = new byte[]{7, 8, 9};
        var expirationImage = new byte[]{1, 2, 3};
        var measureImage = new byte[]{4, 5, 6};
        var saved = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, barcodeImage, expirationImage, measureImage));

        var found = pendingInventoryRepository.findById(saved.id());

        assertThat(found, is(Optional.of(saved)));
        assertThat(found.get().barcodeImage(), is(barcodeImage));
        assertThat(found.get().expirationImage(), is(expirationImage));
        assertThat(found.get().measureImage(), is(measureImage));
    }

    @Test
    @DisplayName("reads a pre-migration document's \"barcode\" string field as legacyBarcode, not barcodeImage")
    void shouldReadLegacyBarcodeFromPreMigrationDocument() {
        mongoTemplate.getCollection("pendingInventory").insertOne(new Document()
                .append("houseId", HOUSE_ID)
                .append("barcode", "5012345678900"));

        var found = pendingInventoryRepository.findAllByHouseId(HOUSE_ID);

        assertThat(found.get(0).legacyBarcode(), is("5012345678900"));
        assertThat(found.get(0).barcodeImage(), is(nullValue()));
    }

    @Test
    void shouldSaveItemWithAllOptionalFieldsMissing() {
        var saved = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, null, null, null));

        assertThat(pendingInventoryRepository.findById(saved.id()), is(Optional.of(saved)));
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        var saved = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, null, null, null));

        assertThat(saved.createdAt(), is(notNullValue()));
        assertThat(saved.updatedAt(), is(notNullValue()));
    }

    @Test
    @DisplayName("findAllByHouseId returns only that house's items, oldest first")
    void shouldFindAllByHouseIdOldestFirst() {
        var first = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, new byte[]{1}, null, null));
        var second = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, new byte[]{2}, null, null));
        pendingInventoryRepository.save(new PendingInventoryItem(null, null, "other-house", new byte[]{3}, null, null));

        var found = pendingInventoryRepository.findAllByHouseId(HOUSE_ID);

        assertThat(found, contains(first, second));
    }

    @Test
    void shouldReturnEmptyWhenIdUnknown() {
        assertThat(pendingInventoryRepository.findById("does-not-exist"), is(Optional.empty()));
    }

    @Test
    void shouldDeleteById() {
        var saved = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, null, null, null));

        pendingInventoryRepository.deleteById(saved.id());

        assertThat(pendingInventoryRepository.findById(saved.id()), is(Optional.empty()));
    }
}
