package org.reciplease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Measure;
import org.reciplease.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.reciplease.utils.ResourceUtils.readTestResource;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@WithMockUser
class InventoryControllerTest {

    @MockBean
    InventoryService inventoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("should create inventory item")
    void shouldCreateInventoryItem() throws Exception {
        final var mockRequestIngredient = Ingredient.builder()
                .id(UUID.fromString("f3aa25a0-5716-4c7d-add5-164396f192fa"))
                .build();
        final var mockRequestItem = InventoryItem.builder()
                .ingredient(mockRequestIngredient)
                .amount(20d)
                .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                .build();
        final var mockResponseIngredient = mockRequestIngredient.toBuilder()
                .name("bread")
                .measure(Measure.ITEMS)
                .build();
        final var mockResponseItem = mockRequestItem.toBuilder()
                .id(UUID.fromString("b465af6e-2465-4436-84c1-14f35db68dbf"))
                .ingredient(mockResponseIngredient)
                .build();

        when(inventoryService.save(mockRequestItem)).thenReturn(mockResponseItem);
        final String createJson = readTestResource(InventoryControllerTest.class, "createItem.json");
        final String savedJson = readTestResource(InventoryControllerTest.class, "savedItem.json");

        mockMvc.perform(post("/api/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(savedJson, true));

        verify(inventoryService).save(mockRequestItem);
    }

    @Nested
    @DisplayName("With item")
    class WithItem {
        private InventoryItem item;

        @BeforeEach
        void setUp() {
            item = InventoryItem.builder()
                    .id(UUID.fromString("b465af6e-2465-4436-84c1-14f35db68dbf"))
                    .ingredient(Ingredient.builder()
                            .id(UUID.fromString("f3aa25a0-5716-4c7d-add5-164396f192fa"))
                            .name("bread")
                            .measure(Measure.ITEMS)
                            .build())
                    .amount(20d)
                    .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                    .build();
        }

        @Test
        @DisplayName("should find item by ID")
        void findById() throws Exception {
            final String itemJson = readTestResource(WithItem.class, "item.json");

            when(inventoryService.findById(item.getId())).thenReturn(Optional.of(item));

            mockMvc.perform(get("/api/inventory/{uuid}", item.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemJson, true));
        }

        @Test
        @DisplayName("should return all items")
        void findAll() throws Exception {
            final String itemsJson = readTestResource(WithItem.class, "items.json");

            when(inventoryService.findAll()).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemsJson, true));
        }

        @Test
        @DisplayName("should return expired items")
        void expiredItems() throws Exception {
            final String itemsJson = readTestResource(WithItem.class, "items.json");

            when(inventoryService.findAllExpired()).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/expired"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemsJson, true));
        }

        @Test
        @DisplayName("should return unexpired items")
        void unexpiredItems() throws Exception {
            final String itemsJson = readTestResource(WithItem.class, "items.json");

            when(inventoryService.findAllUnexpired()).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/unexpired"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemsJson, true));
        }
    }
}
