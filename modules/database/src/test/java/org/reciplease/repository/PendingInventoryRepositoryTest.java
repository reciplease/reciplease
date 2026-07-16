package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MongoAuditingConfig;
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
        var expirationImage = new byte[]{1, 2, 3};
        var measureImage = new byte[]{4, 5, 6};
        var saved = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, "5012345678900", expirationImage, measureImage));

        var found = pendingInventoryRepository.findById(saved.id());

        assertThat(found, is(Optional.of(saved)));
        assertThat(found.get().barcode(), is("5012345678900"));
        assertThat(found.get().expirationImage(), is(expirationImage));
        assertThat(found.get().measureImage(), is(measureImage));
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
        var first = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, "1111111111111", null, null));
        var second = pendingInventoryRepository.save(new PendingInventoryItem(null, null, HOUSE_ID, "2222222222222", null, null));
        pendingInventoryRepository.save(new PendingInventoryItem(null, null, "other-house", "3333333333333", null, null));

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
