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
import static org.hamcrest.Matchers.is;
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
        final var item = InventoryItem.builder()
                .name("bread")
                .measure("ITEMS")
                .amount(10d)
                .expiration(LocalDate.now())
                .barcode("0123456789012")
                .build();

        final var savedItem = item.toBuilder()
                .id(UUID.randomUUID().toString())
                .build();

        when(inventoryRepository.save(item)).thenReturn(savedItem);

        final var actual = inventoryService.save(item);

        assertThat(actual, is(savedItem));
    }

    @Test
    void noItem() {
        final var itemId = UUID.randomUUID().toString();
        when(inventoryRepository.findById(itemId)).thenReturn(Optional.empty());

        final var item = inventoryService.findById(itemId);

        assertThat(item.isEmpty(), is(true));
    }

    @Nested
    class WithItem {

        private InventoryItem item;

        @BeforeEach
        void setUp() {
            item = InventoryItem.builder()
                    .id(UUID.randomUUID().toString())
                    .name("bread")
                    .measure("ITEMS")
                    .amount(10d)
                    .expiration(LocalDate.now())
                    .build();
        }

        @Test
        @DisplayName("should find item by ID")
        void findById() {
            when(inventoryRepository.findById(item.getId())).thenReturn(Optional.of(item));

            final var actual = inventoryService.findById(item.getId());

            assertThat(actual.isPresent(), is(true));
            assertThat(actual.get(), is(item));
        }

        @Test
        void findAll() {
            when(inventoryRepository.findAll()).thenReturn(List.of(item));

            final var actual = inventoryService.findAll();

            assertThat(actual, contains(item));
        }

        @Test
        void shouldFindExpiredInventory() {
            when((inventoryRepository.betweenDates(now.atOffset(ZoneOffset.UTC).toLocalDate())))
                    .thenReturn(List.of(item));

            final var allExpired = inventoryService.findAllExpired();

            assertThat(allExpired, contains(item));
        }

        @Test
        void shouldFindUnexpiredInventory() {
            when(inventoryRepository.expiresAfter(now.atOffset(ZoneOffset.UTC).toLocalDate()))
                    .thenReturn(List.of(item));

            final var allUnexpired = inventoryService.findAllUnexpired();

            assertThat(allUnexpired, contains(item));
        }
    }
}
