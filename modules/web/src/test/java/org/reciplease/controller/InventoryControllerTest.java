package org.reciplease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithHouseMember;
import org.reciplease.configuration.WithHouseOwner;
import org.reciplease.model.InventoryItem;
import org.reciplease.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.reciplease.utils.ResourceUtils.readTestResource;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@WithHouseOwner
@Import(MethodSecurityTestSupport.class)
class InventoryControllerTest {

    private static final String HOUSE_ID = "house-1";

    @MockitoBean
    InventoryService inventoryService;

    @MockitoBean(name = "houseAccess")
    HouseAccess houseAccess;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void stubHouseAccess() {
        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.isMember()).thenReturn(true);
        when(houseAccess.requireHouseId()).thenReturn(HOUSE_ID);
        when(houseAccess.belongsToHeaderHouse(any(InventoryItem.class))).thenReturn(true);
    }

    @Test
    @DisplayName("should create inventory item")
    void shouldCreateInventoryItem() throws Exception {
        var mockRequestItem = new InventoryItem(null, null, HOUSE_ID, "bread", null, "item", 20d, LocalDate.of(2020, Month.JANUARY, 1), "0123456789012");
        var mockResponseItem = mockRequestItem.withId("b465af6e-2465-4436-84c1-14f35db68dbf");

        when(inventoryService.save(mockRequestItem)).thenReturn(mockResponseItem);
        var createJson = readTestResource(InventoryControllerTest.class, "createItem.json");
        var savedJson = readTestResource(InventoryControllerTest.class, "savedItem.json");

        mockMvc.perform(post("/api/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(savedJson, true));

        verify(inventoryService).save(mockRequestItem);
    }

    @Test
    @DisplayName("should create inventory item with an image")
    void shouldCreateInventoryItemWithImage() throws Exception {
        var imageBytes = new byte[]{1, 2, 3};
        var imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        var mockRequestItem = new InventoryItem(null, null, HOUSE_ID, "bread", null, "item", 20d, LocalDate.of(2020, Month.JANUARY, 1), "0123456789012", imageBytes);
        var mockResponseItem = mockRequestItem.withId("b465af6e-2465-4436-84c1-14f35db68dbf");

        when(inventoryService.save(mockRequestItem)).thenReturn(mockResponseItem);

        mockMvc.perform(post("/api/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "bread",
                                  "measure": "item",
                                  "expiration": "2020-01-01",
                                  "amount": 20.0,
                                  "barcode": "0123456789012",
                                  "image": "%s"
                                }""".formatted(imageBase64)))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "uuid": "b465af6e-2465-4436-84c1-14f35db68dbf",
                          "houseId": "%s",
                          "name": "bread",
                          "measure": "item",
                          "expiration": "2020-01-01",
                          "amount": 20.0,
                          "remaining": 20.0,
                          "barcode": "0123456789012",
                          "image": "%s"
                        }""".formatted(HOUSE_ID, imageBase64), true));
    }

    @Test
    @DisplayName("should create inventory item with a brand")
    void shouldCreateInventoryItemWithBrand() throws Exception {
        var mockRequestItem = new InventoryItem(null, null, HOUSE_ID, "tomato ketchup", "Heinz", "item", 20d, LocalDate.of(2020, Month.JANUARY, 1), "0123456789012");
        var mockResponseItem = mockRequestItem.withId("b465af6e-2465-4436-84c1-14f35db68dbf");

        when(inventoryService.save(mockRequestItem)).thenReturn(mockResponseItem);

        mockMvc.perform(post("/api/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "tomato ketchup",
                                  "brand": "Heinz",
                                  "measure": "item",
                                  "expiration": "2020-01-01",
                                  "amount": 20.0,
                                  "barcode": "0123456789012"
                                }"""))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "uuid": "b465af6e-2465-4436-84c1-14f35db68dbf",
                          "houseId": "%s",
                          "name": "tomato ketchup",
                          "brand": "Heinz",
                          "measure": "item",
                          "expiration": "2020-01-01",
                          "amount": 20.0,
                          "remaining": 20.0,
                          "barcode": "0123456789012"
                        }""".formatted(HOUSE_ID), true));
    }

    @Test
    @DisplayName("create is forbidden for read-only members")
    @WithHouseMember
    void createForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(post("/api/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readTestResource(InventoryControllerTest.class, "createItem.json")))
                .andExpect(status().isForbidden());
    }

    @Nested
    @DisplayName("With item")
    class WithItem {
        private InventoryItem item;

        @BeforeEach
        void setUp() {
            item = new InventoryItem("b465af6e-2465-4436-84c1-14f35db68dbf", null, HOUSE_ID, "bread", null, "item", 20d, LocalDate.of(2020, Month.JANUARY, 1), "0123456789012");
        }

        @Test
        @DisplayName("should update item")
        void update() throws Exception {
            var updates = new InventoryItem(null, null, HOUSE_ID, "sourdough", null, "item", 12d, LocalDate.of(2020, Month.JANUARY, 5), "0123456789012");
            var updated = item.withId(item.id());

            when(inventoryService.findById(item.id())).thenReturn(Optional.of(item));
            when(inventoryService.update(item.id(), updates)).thenReturn(Optional.of(updated));

            mockMvc.perform(put("/api/inventory/{uuid}", item.id())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "sourdough",
                                      "measure": "item",
                                      "expiration": "2020-01-05",
                                      "amount": 12.0,
                                      "barcode": "0123456789012"
                                    }"""))
                    .andExpect(status().isOk());

            verify(inventoryService).update(item.id(), updates);
        }

        @Test
        @DisplayName("should 204 when the update drives remaining to zero (item archived and removed)")
        void updateToZeroRemaining() throws Exception {
            var updates = new InventoryItem(null, null, HOUSE_ID, "bread", null, "item", 12d, 0d, LocalDate.of(2020, Month.JANUARY, 5), "0123456789012");

            when(inventoryService.findById(item.id())).thenReturn(Optional.of(item));
            when(inventoryService.update(item.id(), updates)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/inventory/{uuid}", item.id())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "bread",
                                      "measure": "item",
                                      "expiration": "2020-01-05",
                                      "amount": 12.0,
                                      "remaining": 0.0,
                                      "barcode": "0123456789012"
                                    }"""))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should 404 when updating an unknown item")
        void updateUnknown() throws Exception {
            when(inventoryService.findById(item.id())).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/inventory/{uuid}", item.id())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "sourdough",
                                      "measure": "item",
                                      "expiration": "2020-01-05",
                                      "amount": 12.0
                                    }"""))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should delete an item")
        void deleteItem() throws Exception {
            when(inventoryService.findById(item.id())).thenReturn(Optional.of(item));

            mockMvc.perform(delete("/api/inventory/{uuid}", item.id()).with(csrf()))
                    .andExpect(status().isNoContent());

            verify(inventoryService).deleteById(item.id());
        }

        @Test
        @DisplayName("should 404 when deleting an unknown item")
        void deleteUnknown() throws Exception {
            when(inventoryService.findById(item.id())).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/inventory/{uuid}", item.id()).with(csrf()))
                    .andExpect(status().isNotFound());

            verify(inventoryService, never()).deleteById(any());
        }

        @Test
        @DisplayName("should 404 when deleting an item belonging to another house")
        @WithHouseOwner
        void deleteForbiddenForOtherHouse() throws Exception {
            when(houseAccess.belongsToHeaderHouse(any(InventoryItem.class))).thenReturn(false);
            when(inventoryService.findById(item.id())).thenReturn(Optional.of(item));

            mockMvc.perform(delete("/api/inventory/{uuid}", item.id()).with(csrf()))
                    .andExpect(status().isNotFound());

            verify(inventoryService, never()).deleteById(any());
        }

        @Test
        @DisplayName("delete is forbidden for read-only members")
        @WithHouseMember
        void deleteForbiddenForReadOnly() throws Exception {
            when(houseAccess.isOwner()).thenReturn(false);

            mockMvc.perform(delete("/api/inventory/{uuid}", item.id()).with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should find item by ID")
        void findById() throws Exception {
            var itemJson = readTestResource(WithItem.class, "item.json");

            when(inventoryService.findById(item.id())).thenReturn(Optional.of(item));

            mockMvc.perform(get("/api/inventory/{uuid}", item.id()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemJson, true));
        }

        @Test
        @DisplayName("should return all items")
        void findAll() throws Exception {
            var itemsJson = readTestResource(WithItem.class, "items.json");

            when(inventoryService.findAll(HOUSE_ID, false)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemsJson, true));
        }

        @Test
        @DisplayName("should exclude fully-consumed items when excludeFullyConsumed=true")
        void findAllExcludingFullyConsumed() throws Exception {
            var itemsJson = readTestResource(WithItem.class, "items.json");

            when(inventoryService.findAll(HOUSE_ID, true)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory").param("excludeFullyConsumed", "true"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemsJson, true));
        }

        @Test
        @DisplayName("should return expired items")
        void expiredItems() throws Exception {
            var itemsJson = readTestResource(WithItem.class, "items.json");

            when(inventoryService.findAllExpired(HOUSE_ID)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/expired"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemsJson, true));
        }

        @Test
        @DisplayName("should return unexpired items")
        void unexpiredItems() throws Exception {
            var itemsJson = readTestResource(WithItem.class, "items.json");

            when(inventoryService.findAllUnexpired(HOUSE_ID)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/inventory/unexpired"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(itemsJson, true));
        }
    }

}
