package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class InventoryServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private IngredientRepository ingredientRepository;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        inventoryService = new InventoryService(inventoryRepository, ingredientRepository);
    }

    @Test
    @DisplayName("should save item")
    void save() {
        final var ingredientId = UUID.randomUUID();
        final var ingredient = Ingredient.builder()
                .uuid(ingredientId)
                .build();

        final var itemDto = InventoryItemDto.builder()
                .ingredientId(ingredientId)
                .amount(10d)
                .expiration(LocalDate.now())
                .build();

        final var item = InventoryItem.builder()
                .ingredient(ingredient)
                .amount(itemDto.getAmount())
                .expiration(itemDto.getExpiration())
                .build();

        final var savedItem = item.toBuilder()
                .uuid(UUID.randomUUID())
                .build();

        final var savedItemDto = InventoryItemDto.from(savedItem);

        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(ingredient));
        when(inventoryRepository.save(item)).thenReturn(savedItem);

        final var actual = inventoryService.save(itemDto);

        assertThat(actual, is(savedItemDto));
    }

    @Test
    void noIngredient() {
        final var ingredientId = UUID.randomUUID();
        final var itemDto = InventoryItemDto.builder()
                .ingredientId(ingredientId)
                .amount(10d)
                .expiration(LocalDate.now())
                .build();

        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.empty());

        final var illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> inventoryService.save(itemDto));

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

        private UUID itemId;
        private InventoryItem item;
        private InventoryItemDto itemDto;

        @BeforeEach
        void setUp() {
            itemId = UUID.randomUUID();
            item = InventoryItem.builder()
                    .uuid(itemId)
                    .ingredient(Ingredient.builder()
                            .uuid(UUID.randomUUID())
                            .build())
                    .amount(10d)
                    .expiration(LocalDate.now())
                    .build();

            itemDto = InventoryItemDto.from(item);
        }

        @Test
        @DisplayName("should find item by ID")
        void findById() {
            when(inventoryRepository.findById(itemId)).thenReturn(Optional.of(item));

            final var actual = inventoryService.findById(itemId);

            assertThat(actual.isPresent(), is(true));
            assertThat(actual.get(), is(itemDto));
        }

        @Test
        void findAll() {
            when(inventoryRepository.findAll()).thenReturn(List.of(item));

            final var actual = inventoryService.findAll();

            assertThat(actual, contains(itemDto));
        }
    }
}