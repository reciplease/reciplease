package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.IngredientRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@MockitoSettings
class InventoryServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    private Instant now;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        inventoryService = new InventoryService(inventoryRepository, ingredientRepository, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("should save item")
    void save() {
        final var ingredient = Ingredient.builder()
                .id(UUID.randomUUID())
                .build();

        final var item = InventoryItem.builder()
                .ingredient(ingredient)
                .amount(10d)
                .expiration(LocalDate.now())
                .build();

        final var savedItem = item.toBuilder()
                .id(UUID.randomUUID())
                .build();

        when(ingredientRepository.findById(ingredient.getId())).thenReturn(Optional.of(ingredient));
        when(inventoryRepository.save(item)).thenReturn(savedItem);

        final var actual = inventoryService.save(item);

        assertThat(actual, is(savedItem));
    }

    @Test
    void noIngredient() {
        final Ingredient ingredient = Ingredient.builder().id(UUID.randomUUID()).build();
        final var item = InventoryItem.builder()
                .ingredient(ingredient)
                .amount(10d)
                .expiration(LocalDate.now())
                .build();

        when(ingredientRepository.findById(ingredient.getId())).thenReturn(Optional.empty());

        final var illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> inventoryService.save(item));

        assertThat(illegalArgumentException.getMessage(), is("Ingredient does not exist"));
    }

    @Test
    void noItem() {
        final var itemId = UUID.randomUUID();
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
                    .id(UUID.randomUUID())
                    .ingredient(Ingredient.builder()
                            .id(UUID.randomUUID())
                            .build())
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
