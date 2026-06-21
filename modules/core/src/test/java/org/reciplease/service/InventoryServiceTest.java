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

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        inventoryService = new InventoryService(inventoryRepository, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("should save item")
    void save() {
        var item = new InventoryItem(null, null, "bread", "ITEMS", 10d, LocalDate.now(), "0123456789012");
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
            item = new InventoryItem(UUID.randomUUID().toString(), null, "bread", "ITEMS", 10d, LocalDate.now(), null);
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
            var expiredItem = new InventoryItem(UUID.randomUUID().toString(), null, "milk", "MILLILITRES", 500d, LocalDate.now().minusDays(1), null);
            var today = now.atOffset(ZoneOffset.UTC).toLocalDate();

            when(inventoryRepository.expiresAfter(today)).thenReturn(List.of(item));
            when(inventoryRepository.betweenDates(today)).thenReturn(List.of(expiredItem));

            var actual = inventoryService.findAll();

            assertThat(actual, contains(item, expiredItem));
        }

        @Test
        void shouldFindExpiredInventory() {
            when((inventoryRepository.betweenDates(now.atOffset(ZoneOffset.UTC).toLocalDate())))
                    .thenReturn(List.of(item));

            var allExpired = inventoryService.findAllExpired();

            assertThat(allExpired, contains(item));
        }

        @Test
        void shouldFindUnexpiredInventory() {
            when(inventoryRepository.expiresAfter(now.atOffset(ZoneOffset.UTC).toLocalDate()))
                    .thenReturn(List.of(item));

            var allUnexpired = inventoryService.findAllUnexpired();

            assertThat(allUnexpired, contains(item));
        }

        @Test
        @DisplayName("update merges editable fields but preserves id, createdBy and createdAt")
        void shouldUpdatePreservingAuditFields() {
            var createdAt = Instant.now().minusSeconds(60);
            var existing = new InventoryItem(item.id(), "someone", "bread", "ITEMS", 10d, LocalDate.now(), null,
                    null, createdAt, createdAt);
            var updates = new InventoryItem(null, null, "sourdough", "ITEMS", 12d, LocalDate.now().plusDays(3),
                    "0123456789012", new byte[]{1, 2, 3});

            when(inventoryRepository.findById(item.id())).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var updated = inventoryService.update(item.id(), updates);

            assertThat(updated.id(), is(existing.id()));
            assertThat(updated.createdBy(), is(existing.createdBy()));
            assertThat(updated.createdAt(), is(existing.createdAt()));
            assertThat(updated.name(), is("sourdough"));
            assertThat(updated.amount(), is(12d));
            assertThat(updated.expiration(), is(updates.expiration()));
            assertThat(updated.barcode(), is("0123456789012"));
            assertThat(updated.image(), is(equalTo(updates.image())));
        }
    }

    @Test
    @DisplayName("update throws when the item does not exist")
    void shouldThrowWhenUpdatingUnknownItem() {
        var itemId = UUID.randomUUID().toString();
        when(inventoryRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.update(itemId, new InventoryItem(null, null, "bread", "ITEMS", 10d, LocalDate.now(), null)));

        verify(inventoryRepository, never()).save(any());
    }
}
