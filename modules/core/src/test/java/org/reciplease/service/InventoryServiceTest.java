package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.InventoryRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class InventoryServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;
    private Instant now;
    private static final String HOUSE_ID = "house-1";

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        inventoryService = new InventoryService(inventoryRepository, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("should save item")
    void save() {
        var item = new InventoryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), "0123456789012");
        var savedItem = item.withId(UUID.randomUUID().toString());

        when(inventoryRepository.save(item)).thenReturn(savedItem);

        var actual = inventoryService.save(item);

        assertThat(actual, is(savedItem));
    }

    @Test
    void noItem() {
        var itemId = UUID.randomUUID().toString();
        when(inventoryRepository.findById(itemId)).thenReturn(Optional.empty());

        var item = inventoryService.findById(itemId);

        assertThat(item.isEmpty(), is(true));
    }

    @Nested
    class WithItem {

        private InventoryItem item;

        @BeforeEach
        void setUp() {
            item = new InventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), null);
        }

        @Test
        @DisplayName("should find item by ID")
        void findById() {
            when(inventoryRepository.findById(item.id())).thenReturn(Optional.of(item));

            var actual = inventoryService.findById(item.id());

            assertThat(actual.isPresent(), is(true));
            assertThat(actual.get(), is(item));
        }

        @Test
        @DisplayName("findAll returns unexpired items followed by expired items")
        void findAll() {
            var expiredItem = new InventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "milk", null, "MILLILITRES", 500d, LocalDate.now().minusDays(1), null);
            var today = now.atOffset(ZoneOffset.UTC).toLocalDate();

            when(inventoryRepository.expiresAfter(HOUSE_ID, today)).thenReturn(List.of(item));
            when(inventoryRepository.betweenDates(HOUSE_ID, today)).thenReturn(List.of(expiredItem));

            var actual = inventoryService.findAll(HOUSE_ID);

            assertThat(actual, contains(item, expiredItem));
        }

        @Test
        @DisplayName("findAll(houseId, excludeFullyConsumed=false) includes items with nothing remaining")
        void findAllIncludesFullyConsumedByDefault() {
            var consumedItem = item.withRemaining(0d);
            var today = now.atOffset(ZoneOffset.UTC).toLocalDate();
            when(inventoryRepository.expiresAfter(HOUSE_ID, today)).thenReturn(List.of(consumedItem));
            when(inventoryRepository.betweenDates(HOUSE_ID, today)).thenReturn(List.of());

            var actual = inventoryService.findAll(HOUSE_ID, false);

            assertThat(actual, contains(consumedItem));
        }

        @Test
        @DisplayName("findAll(houseId, excludeFullyConsumed=true) drops items with nothing remaining")
        void findAllCanExcludeFullyConsumed() {
            var consumedItem = item.withRemaining(0d);
            var today = now.atOffset(ZoneOffset.UTC).toLocalDate();
            when(inventoryRepository.expiresAfter(HOUSE_ID, today)).thenReturn(List.of(item, consumedItem));
            when(inventoryRepository.betweenDates(HOUSE_ID, today)).thenReturn(List.of());

            var actual = inventoryService.findAll(HOUSE_ID, true);

            assertThat(actual, contains(item));
        }

        @Test
        void shouldFindExpiredInventory() {
            when((inventoryRepository.betweenDates(HOUSE_ID, now.atOffset(ZoneOffset.UTC).toLocalDate())))
                    .thenReturn(List.of(item));

            var allExpired = inventoryService.findAllExpired(HOUSE_ID);

            assertThat(allExpired, contains(item));
        }

        @Test
        void shouldFindUnexpiredInventory() {
            when(inventoryRepository.expiresAfter(HOUSE_ID, now.atOffset(ZoneOffset.UTC).toLocalDate()))
                    .thenReturn(List.of(item));

            var allUnexpired = inventoryService.findAllUnexpired(HOUSE_ID);

            assertThat(allUnexpired, contains(item));
        }

        @Test
        @DisplayName("update merges editable fields but preserves id, createdBy and createdAt")
        void shouldUpdatePreservingAuditFields() {
            var createdAt = Instant.now().minusSeconds(60);
            var existing = new InventoryItem(item.id(), "someone", HOUSE_ID, "bread", null, "ITEMS", 10d, 4d, LocalDate.now(), null,
                    null, createdAt, createdAt);
            var updates = new InventoryItem(null, null, HOUSE_ID, "sourdough", "Warburtons", "ITEMS", 12d, 4d, LocalDate.now().plusDays(3),
                    "0123456789012", new byte[]{1, 2, 3});

            when(inventoryRepository.findById(item.id())).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var updated = inventoryService.update(item.id(), updates).orElseThrow();

            assertThat(updated.id(), is(existing.id()));
            assertThat(updated.createdBy(), is(existing.createdBy()));
            assertThat(updated.createdAt(), is(existing.createdAt()));
            assertThat(updated.name(), is("sourdough"));
            assertThat(updated.brand(), is("Warburtons"));
            assertThat(updated.amount(), is(12d));
            assertThat(updated.remaining(), is(4d));
            assertThat(updated.expiration(), is(updates.expiration()));
            assertThat(updated.barcode(), is("0123456789012"));
            assertThat(updated.image(), is(equalTo(updates.image())));
        }

        @Test
        @DisplayName("update resets remaining to the new amount if the caller doesn't resend it")
        void shouldResetRemainingWhenCallerOmitsIt() {
            var existing = new InventoryItem(item.id(), null, HOUSE_ID, "bread", null, "ITEMS", 10d, 4d, LocalDate.now(), null);
            // Built via the overload that doesn't mention `remaining` at all, so it defaults to
            // the new `amount` rather than carrying over the caller's intent to preserve it.
            var updates = new InventoryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 12d, LocalDate.now(), null);

            when(inventoryRepository.findById(item.id())).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var updated = inventoryService.update(item.id(), updates).orElseThrow();

            assertThat(updated.remaining(), is(12d));
        }

        @Test
        @DisplayName("update archives and deletes instead of saving when the new remaining is zero")
        void shouldArchiveWhenUpdateZerosRemaining() {
            var existing = new InventoryItem(item.id(), null, HOUSE_ID, "bread", null, "ITEMS", 10d, 4d, LocalDate.now(), null);
            var updates = new InventoryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, 0d, LocalDate.now(), null);

            when(inventoryRepository.findById(item.id())).thenReturn(Optional.of(existing));

            var updated = inventoryService.update(item.id(), updates);

            assertThat(updated.isEmpty(), is(true));
            verify(inventoryRepository).deleteById(item.id());
            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("update throws when the item does not exist")
    void shouldThrowWhenUpdatingUnknownItem() {
        var itemId = UUID.randomUUID().toString();
        when(inventoryRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.update(itemId, new InventoryItem(null, null, HOUSE_ID, "bread", null, "ITEMS", 10d, LocalDate.now(), null)));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteById delegates to the repository")
    void shouldDeleteById() {
        var itemId = UUID.randomUUID().toString();

        inventoryService.deleteById(itemId);

        verify(inventoryRepository).deleteById(itemId);
    }

    @Nested
    class Consume {
        @Test
        @DisplayName("reduces remaining by the given amount")
        void reducesRemaining() {
            var existing = new InventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "bread", null, "ITEMS", 10d, 4d, LocalDate.now(), null);

            when(inventoryRepository.findById(existing.id())).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var updated = inventoryService.consume(existing.id(), 3d).orElseThrow();

            assertThat(updated.remaining(), is(1d));
        }

        @Test
        @DisplayName("clamps remaining at zero and archives/deletes rather than saving a zeroed item")
        void clampsAtZeroAndArchives() {
            var existing = new InventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "bread", null, "ITEMS", 10d, 2d, LocalDate.now(), null);

            when(inventoryRepository.findById(existing.id())).thenReturn(Optional.of(existing));

            var updated = inventoryService.consume(existing.id(), 5d);

            assertThat(updated.isEmpty(), is(true));
            verify(inventoryRepository).deleteById(existing.id());
            verify(inventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when the item does not exist")
        void shouldThrowWhenItemMissing() {
            var itemId = UUID.randomUUID().toString();
            when(inventoryRepository.findById(itemId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> inventoryService.consume(itemId, 1d));

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("archiveAllZeroRemainingItems deletes (and thereby archives) every zero-remaining item found")
    void shouldArchiveAllZeroRemainingItems() {
        var first = new InventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "bread", null, "ITEMS", 10d, 0d, LocalDate.now(), null);
        var second = new InventoryItem(UUID.randomUUID().toString(), null, HOUSE_ID, "milk", null, "MILLILITRES", 500d, 0d, LocalDate.now(), null);
        when(inventoryRepository.findAllZeroRemaining()).thenReturn(List.of(first, second));

        inventoryService.archiveAllZeroRemainingItems();

        verify(inventoryRepository).deleteById(first.id());
        verify(inventoryRepository).deleteById(second.id());
    }
}
