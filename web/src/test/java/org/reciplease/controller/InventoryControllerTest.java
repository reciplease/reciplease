package org.reciplease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @MockBean
    InventoryService inventoryService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldCreateInventoryItem() throws Exception {
        final var item = InventoryItem.builder()
                .ingredient(Ingredient.builder()
                        .randomUUID()
                        .build())
                .amount(20d)
                .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                .build();

        final var savedItem = item.toBuilder()
                .randomUUID()
                .build();

        final var itemDto = InventoryItemDto.from(item);
        final var savedItemDto = InventoryItemDto.from(savedItem);

        when(inventoryService.save(item)).thenReturn(savedItem);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(savedItemDto)));
    }

    @Nested
    class WithItem {
        private InventoryItemDto itemDto;
        private InventoryItem item;

        @BeforeEach
        void setUp() {
            item = InventoryItem.builder()
                    .randomUUID()
                    .ingredient(Ingredient.builder()
                            .randomUUID()
                            .build())
                    .amount(20d)
                    .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                    .build();
            itemDto = InventoryItemDto.from(item);
        }

        @Test
        @DisplayName("should find item by ID")
        void findById() throws Exception {
            when(inventoryService.findById(item.getUuid())).thenReturn(Optional.of(item));

            mockMvc.perform(get("/api/inventory/{uuid}", item.getUuid()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemDto)));
        }

        @Test
        @DisplayName("should return all items")
        void findAll() throws Exception {
            when(inventoryService.findAll()).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
        }

        @Test
        @DisplayName("should return expired items")
        void expiredItems() throws Exception {
            when(inventoryService.findAllExpired()).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/expired"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
        }

        @Test
        @DisplayName("should return unexpired items")
        void unexpiredItems() throws Exception {
            when(inventoryService.findAllUnexpired()).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/unexpired"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
        }
    }
}