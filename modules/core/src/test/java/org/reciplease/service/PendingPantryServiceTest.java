package org.reciplease.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.PantryItem;
import org.reciplease.model.PendingPantryItem;
import org.reciplease.repository.PantryRepository;
import org.reciplease.repository.PendingPantryRepository;

@MockitoSettings
class PendingPantryServiceTest {
    private static final String HOUSE_ID = "house-1";

    @Mock
    private PendingPantryRepository pendingPantryRepository;

    @Mock
    private PantryRepository pantryRepository;

    private PendingPantryService pendingPantryService;

    @BeforeEach
    void setUp() {
        pendingPantryService = new PendingPantryService(pendingPantryRepository, pantryRepository);
    }

    @Test
    @DisplayName("should save pending item")
    void save() {
        var pending =
                new PendingPantryItem(null, null, HOUSE_ID, "0123456789012".getBytes(), new byte[] {1}, new byte[] {2});
        var saved = pending.withId(UUID.randomUUID().toString());

        when(pendingPantryRepository.save(pending)).thenReturn(saved);

        var actual = pendingPantryService.save(pending);

        assertThat(actual, is(saved));
    }

    @Test
    @DisplayName("should find pending item by ID")
    void findById() {
        var pending = new PendingPantryItem(UUID.randomUUID().toString(), null, HOUSE_ID, null, null, null);
        when(pendingPantryRepository.findById(pending.id())).thenReturn(Optional.of(pending));

        var actual = pendingPantryService.findById(pending.id());

        assertThat(actual, is(Optional.of(pending)));
    }

    @Test
    @DisplayName("should list all pending items for a house")
    void findAll() {
        var pending = new PendingPantryItem(
                UUID.randomUUID().toString(), null, HOUSE_ID, "0123456789012".getBytes(), null, null);
        when(pendingPantryRepository.findAllByHouseId(HOUSE_ID)).thenReturn(List.of(pending));
        when(pantryRepository.findById(pending.id())).thenReturn(Optional.empty());

        var actual = pendingPantryService.findAll(HOUSE_ID);

        assertThat(actual, contains(pending));
    }

    @Test
    @DisplayName("findAll silently deletes and excludes pending items already completed into pantry")
    void findAllSweepsCompletedPendingItems() {
        var pending = new PendingPantryItem(
                UUID.randomUUID().toString(), null, HOUSE_ID, "0123456789012".getBytes(), null, null);
        var alreadyCompleted = new PendingPantryItem(
                UUID.randomUUID().toString(), null, HOUSE_ID, "9999999999999".getBytes(), null, null);
        var completedItem = new PantryItem(
                alreadyCompleted.id(), null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), "9999999999999");

        when(pendingPantryRepository.findAllByHouseId(HOUSE_ID)).thenReturn(List.of(pending, alreadyCompleted));
        when(pantryRepository.findById(pending.id())).thenReturn(Optional.empty());
        when(pantryRepository.findById(alreadyCompleted.id())).thenReturn(Optional.of(completedItem));

        var actual = pendingPantryService.findAll(HOUSE_ID);

        assertThat(actual, contains(pending));
        verify(pendingPantryRepository).deleteById(alreadyCompleted.id());
        verify(pendingPantryRepository, never()).deleteById(pending.id());
    }

    @Test
    @DisplayName("deleteById delegates to the repository")
    void shouldDeleteById() {
        var pendingId = UUID.randomUUID().toString();

        pendingPantryService.deleteById(pendingId);

        verify(pendingPantryRepository).deleteById(pendingId);
    }

    @Test
    @DisplayName("complete saves the pantry item under the pending item's id, then deletes the pending item")
    void completeSavesThenDeletes() {
        var pendingId = UUID.randomUUID().toString();
        var pending = new PendingPantryItem(pendingId, null, HOUSE_ID, "0123456789012".getBytes(), null, null);
        var item = new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var savedItem = item.withId(pendingId);

        when(pendingPantryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(pantryRepository.save(item.withId(pendingId))).thenReturn(savedItem);

        var actual = pendingPantryService.complete(pendingId, item);

        assertThat(actual, is(savedItem));
        final InOrder inOrder = Mockito.inOrder(pantryRepository, pendingPantryRepository);
        inOrder.verify(pantryRepository).save(item.withId(pendingId));
        inOrder.verify(pendingPantryRepository).deleteById(pendingId);
    }

    @Test
    @DisplayName("complete carries the pending capture's creation metadata onto the pantry item")
    void completeCarriesAuditFields() {
        var pendingId = UUID.randomUUID().toString();
        var capturedAt = Instant.parse("2026-07-01T10:15:30Z");
        var pending = new PendingPantryItem(
                pendingId, "user-1", HOUSE_ID, "0123456789012".getBytes(), null, null, null, capturedAt, capturedAt);
        var item = new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var expectedSave = new PantryItem(
                pendingId,
                "user-1",
                HOUSE_ID,
                item.name(),
                null,
                item.measure(),
                item.amount(),
                item.remaining(),
                item.expiration(),
                item.barcode(),
                item.image(),
                capturedAt,
                null);

        when(pendingPantryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(pantryRepository.save(expectedSave)).thenReturn(expectedSave);

        var actual = pendingPantryService.complete(pendingId, item);

        assertThat(actual, is(expectedSave));
        verify(pantryRepository).save(expectedSave);
    }

    @Test
    @DisplayName("complete refuses to overwrite a pantry item belonging to another house")
    void completeRefusesCrossHouseOverwrite() {
        var pendingId = UUID.randomUUID().toString();
        var pending = new PendingPantryItem(pendingId, null, HOUSE_ID, "0123456789012".getBytes(), null, null);
        var item = new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var otherHouseItem =
                new PantryItem(pendingId, null, "house-2", "milk", null, "LITRES", 1d, LocalDate.now(), null);

        when(pendingPantryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(pantryRepository.findById(pendingId)).thenReturn(Optional.of(otherHouseItem));

        assertThrows(IllegalArgumentException.class, () -> pendingPantryService.complete(pendingId, item));

        verify(pantryRepository, never()).save(any());
        verify(pendingPantryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("complete overwrites a same-house pantry item under the same id (retry after a failed delete)")
    void completeRetriesSameHouseOverwrite() {
        var pendingId = UUID.randomUUID().toString();
        var pending = new PendingPantryItem(pendingId, null, HOUSE_ID, "0123456789012".getBytes(), null, null);
        var item = new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var previousAttempt = item.withId(pendingId);

        when(pendingPantryRepository.findById(pendingId)).thenReturn(Optional.of(pending));
        when(pantryRepository.findById(pendingId)).thenReturn(Optional.of(previousAttempt));
        when(pantryRepository.save(item.withId(pendingId))).thenReturn(previousAttempt);

        var actual = pendingPantryService.complete(pendingId, item);

        assertThat(actual, is(previousAttempt));
        verify(pendingPantryRepository).deleteById(pendingId);
    }

    @Test
    @DisplayName("complete throws when the pending item does not exist and saves nothing")
    void completeThrowsWhenPendingMissing() {
        var pendingId = UUID.randomUUID().toString();
        var item = new PantryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), null);
        when(pendingPantryRepository.findById(pendingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pendingPantryService.complete(pendingId, item));

        verify(pantryRepository, never()).save(any());
        verify(pendingPantryRepository, never()).deleteById(any());
    }
}
