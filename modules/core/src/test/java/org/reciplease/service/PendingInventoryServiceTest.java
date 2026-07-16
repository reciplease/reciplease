package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PendingInventoryItem;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.PendingInventoryRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class PendingInventoryServiceTest {
    private static final String HOUSE_ID = "house-1";

    @Mock
    private PendingInventoryRepository pendingInventoryRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    private PendingInventoryService pendingInventoryService;

    @BeforeEach
    void setUp() {
        pendingInventoryService = new PendingInventoryService(pendingInventoryRepository, inventoryRepository);
    }

    @Test
    @DisplayName("should save pending item")
    void save() {
        var pending = new PendingInventoryItem(null, null, HOUSE_ID, "0123456789012", new byte[]{1}, new byte[]{2});
        var saved = pending.withId(UUID.randomUUID().toString());

        when(pendingInventoryRepository.save(pending)).thenReturn(saved);

        var actual = pendingInventoryService.save(pending);

        assertThat(actual, is(saved));
    }

    @Test
    @DisplayName("should find pending item by ID")
    void findById() {
        var pending = new PendingInventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, null, null, null);
        when(pendingInventoryRepository.findById(pending.id())).thenReturn(Optional.of(pending));

        var actual = pendingInventoryService.findById(pending.id());

        assertThat(actual, is(Optional.of(pending)));
    }

    @Test
    @DisplayName("should list all pending items for a house")
    void findAll() {
        var pending = new PendingInventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "0123456789012", null, null);
        when(pendingInventoryRepository.findAllByHouseId(HOUSE_ID)).thenReturn(List.of(pending));
        when(inventoryRepository.findById(pending.id())).thenReturn(Optional.empty());

        var actual = pendingInventoryService.findAll(HOUSE_ID);

        assertThat(actual, contains(pending));
    }

    @Test
    @DisplayName("findAll silently deletes and excludes pending items already completed into inventory")
    void findAllSweepsCompletedPendingItems() {
        var pending = new PendingInventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "0123456789012", null, null);
        var alreadyCompleted = new PendingInventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "9999999999999", null, null);
        var completedItem = new InventoryItem(alreadyCompleted.id(), null, HOUSE_ID, "bread", "ITEMS", 10d, LocalDate.now(), "9999999999999");

        when(pendingInventoryRepository.findAllByHouseId(HOUSE_ID)).thenReturn(List.of(pending, alreadyCompleted));
        when(inventoryRepository.findById(pending.id())).thenReturn(Optional.empty());
        when(inventoryRepository.findById(alreadyCompleted.id())).thenReturn(Optional.of(completedItem));

        var actual = pendingInventoryService.findAll(HOUSE_ID);

        assertThat(actual, contains(pending));
        verify(pendingInventoryRepository).deleteById(alreadyCompleted.id());
        verify(pendingInventoryRepository, never()).deleteById(pending.id());
    }

    @Test
    @DisplayName("deleteById delegates to the repository")
    void shouldDeleteById() {
        var pendingId = UUID.randomUUID().toString();

        pendingInventoryService.deleteById(pendingId);

        verify(pendingInventoryRepository).deleteById(pendingId);
    }

    @Test
    @DisplayName("complete saves the inventory item under the pending item's id, then deletes the pending item")
    void completeSavesThenDeletes() {
        var pendingId = UUID.randomUUID().toString();
        var pending = new PendingInventoryItem(pendingId, null, HOUSE_ID, "0123456789012", null, null);
        var item = new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var savedItem = item.withId(pendingId);

        when(pendingInventoryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(inventoryRepository.save(item.withId(pendingId))).thenReturn(savedItem);

        var actual = pendingInventoryService.complete(pendingId, item);

        assertThat(actual, is(savedItem));
        final InOrder inOrder = Mockito.inOrder(inventoryRepository, pendingInventoryRepository);
        inOrder.verify(inventoryRepository).save(item.withId(pendingId));
        inOrder.verify(pendingInventoryRepository).deleteById(pendingId);
    }

    @Test
    @DisplayName("complete carries the pending capture's creation metadata onto the inventory item")
    void completeCarriesAuditFields() {
        var pendingId = UUID.randomUUID().toString();
        var capturedAt = Instant.parse("2026-07-01T10:15:30Z");
        var pending = new PendingInventoryItem(pendingId, "user-1", HOUSE_ID, "0123456789012", null, null, capturedAt, capturedAt);
        var item = new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var expectedSave = new InventoryItem(pendingId, "user-1", HOUSE_ID, item.name(), item.measure(),
                item.amount(), item.remaining(), item.expiration(), item.barcode(), item.image(), capturedAt, null);

        when(pendingInventoryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(inventoryRepository.save(expectedSave)).thenReturn(expectedSave);

        var actual = pendingInventoryService.complete(pendingId, item);

        assertThat(actual, is(expectedSave));
        verify(inventoryRepository).save(expectedSave);
    }

    @Test
    @DisplayName("complete refuses to overwrite an inventory item belonging to another house")
    void completeRefusesCrossHouseOverwrite() {
        var pendingId = UUID.randomUUID().toString();
        var pending = new PendingInventoryItem(pendingId, null, HOUSE_ID, "0123456789012", null, null);
        var item = new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var otherHouseItem = new InventoryItem(pendingId, null, "house-2", "milk", "LITRES", 1d, LocalDate.now(), null);

        when(pendingInventoryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(inventoryRepository.findById(pendingId)).thenReturn(Optional.of(otherHouseItem));

        assertThrows(IllegalArgumentException.class, () -> pendingInventoryService.complete(pendingId, item));

        verify(inventoryRepository, never()).save(any());
        verify(pendingInventoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("complete overwrites a same-house inventory item under the same id (retry after a failed delete)")
    void completeRetriesSameHouseOverwrite() {
        var pendingId = UUID.randomUUID().toString();
        var pending = new PendingInventoryItem(pendingId, null, HOUSE_ID, "0123456789012", null, null);
        var item = new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var previousAttempt = item.withId(pendingId);

        when(pendingInventoryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(inventoryRepository.findById(pendingId)).thenReturn(Optional.of(previousAttempt));
        when(inventoryRepository.save(item.withId(pendingId))).thenReturn(previousAttempt);

        var actual = pendingInventoryService.complete(pendingId, item);

        assertThat(actual, is(previousAttempt));
        verify(pendingInventoryRepository).deleteById(pendingId);
    }

    @Test
    @DisplayName("complete throws when the pending item does not exist and saves nothing")
    void completeThrowsWhenPendingMissing() {
        var pendingId = UUID.randomUUID().toString();
        var item = new InventoryItem(null, null, HOUSE_ID, "bread", "ITEMS", 10d, LocalDate.now(), null);
        when(pendingInventoryRepository.findById(pendingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pendingInventoryService.complete(pendingId, item));

        verify(inventoryRepository, never()).save(any());
        verify(pendingInventoryRepository, never()).deleteById(any());
    }
}
