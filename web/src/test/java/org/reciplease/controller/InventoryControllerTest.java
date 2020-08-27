package org.reciplease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
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
        final var item = InventoryItemDto.builder()
                .ingredientId(UUID.randomUUID())
                .amount(20d)
                .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                .build();

        final var savedItem = item.toBuilder()
                .id(UUID.randomUUID())
                .build();

        when(inventoryService.save(item)).thenReturn(savedItem);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(savedItem)));
    }

    @Nested
    class WithItem {
        private UUID itemId;
        private InventoryItemDto item;

        @BeforeEach
        void setUp() {
            itemId = UUID.randomUUID();
            item = InventoryItemDto.builder()
                    .id(itemId)
                    .ingredientId(UUID.randomUUID())
                    .amount(20d)
                    .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                    .build();
        }

        @Test
        @DisplayName("should find item by ID")
        void findById() throws Exception {
            when(inventoryService.findById(itemId)).thenReturn(Optional.of(item));

            mockMvc.perform(get("/api/inventory/" + itemId))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(item)));
        }

        @Test
        @DisplayName("should return all items")
        void findAll() throws Exception {
            final var items = List.of(item);
            when(inventoryService.findAll()).thenReturn(items);

            mockMvc.perform(get("/api/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(items)));
        }
    }
}