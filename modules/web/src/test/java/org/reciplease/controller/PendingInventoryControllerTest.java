package org.reciplease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithHouseMember;
import org.reciplease.configuration.WithHouseOwner;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PendingInventoryItem;
import org.reciplease.service.InventoryService;
import org.reciplease.service.PendingInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// InventoryController is loaded alongside so the tests prove the literal `pending` segment
// wins over its `api/inventory/{uuid}` mapping rather than being captured as a uuid.
@WebMvcTest({PendingInventoryController.class, InventoryController.class})
@WithHouseOwner
@Import(MethodSecurityTestSupport.class)
class PendingInventoryControllerTest {

    private static final String HOUSE_ID = "house-1";
    private static final String PENDING_ID = "b465af6e-2465-4436-84c1-14f35db68dbf";

    @MockitoBean
    PendingInventoryService pendingInventoryService;

    @MockitoBean
    InventoryService inventoryService;

    @MockitoBean(name = "houseAccess")
    HouseAccess houseAccess;

    @Autowired
    private MockMvc mockMvc;

    private final byte[] expirationImageBytes = new byte[]{1, 2, 3};
    private final byte[] measureImageBytes = new byte[]{4, 5, 6};
    private final String expirationImageBase64 = Base64.getEncoder().encodeToString(expirationImageBytes);
    private final String measureImageBase64 = Base64.getEncoder().encodeToString(measureImageBytes);

    @BeforeEach
    void stubHouseAccess() {
        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.isMember()).thenReturn(true);
        when(houseAccess.requireHouseId()).thenReturn(HOUSE_ID);
        when(houseAccess.belongsToHeaderHouse(any(PendingInventoryItem.class))).thenReturn(true);
    }

    @Test
    @DisplayName("should create a pending item with barcode and both images")
    void shouldCreatePendingItem() throws Exception {
        var requestItem = new PendingInventoryItem(null, null, HOUSE_ID, "0123456789012", expirationImageBytes, measureImageBytes);
        var savedItem = requestItem.withId(PENDING_ID);

        when(pendingInventoryService.save(requestItem)).thenReturn(savedItem);

        mockMvc.perform(post("/api/inventory/pending")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "barcode": "0123456789012",
                                  "expirationImage": "%s",
                                  "measureImage": "%s"
                                }""".formatted(expirationImageBase64, measureImageBase64)))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "uuid": "%s",
                          "houseId": "%s",
                          "barcode": "0123456789012",
                          "expirationImage": "%s",
                          "measureImage": "%s"
                        }""".formatted(PENDING_ID, HOUSE_ID, expirationImageBase64, measureImageBase64), true));
    }

    @Test
    @DisplayName("should create a pending item with every field skipped")
    void shouldCreateEmptyPendingItem() throws Exception {
        var requestItem = new PendingInventoryItem(null, null, HOUSE_ID, null, null, null);
        var savedItem = requestItem.withId(PENDING_ID);

        when(pendingInventoryService.save(requestItem)).thenReturn(savedItem);

        mockMvc.perform(post("/api/inventory/pending")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "uuid": "%s",
                          "houseId": "%s"
                        }""".formatted(PENDING_ID, HOUSE_ID), true));
    }

    @Test
    @DisplayName("create ignores a caller-supplied uuid — ids are always server-generated")
    void createIgnoresBodyUuid() throws Exception {
        var requestItem = new PendingInventoryItem(null, null, HOUSE_ID, "0123456789012", null, null);
        when(pendingInventoryService.save(requestItem)).thenReturn(requestItem.withId(PENDING_ID));

        mockMvc.perform(post("/api/inventory/pending")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "uuid": "attacker-chosen-id",
                                  "barcode": "0123456789012"
                                }"""))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "uuid": "%s"
                        }""".formatted(PENDING_ID)));

        verify(pendingInventoryService).save(requestItem);
    }

    @Test
    @DisplayName("create is forbidden for read-only members")
    @WithHouseMember
    void createForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(post("/api/inventory/pending")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should list the house's pending items")
    void shouldListPendingItems() throws Exception {
        var pending = new PendingInventoryItem(PENDING_ID, null, HOUSE_ID, "0123456789012", expirationImageBytes, null);

        when(pendingInventoryService.findAll(HOUSE_ID)).thenReturn(List.of(pending));

        mockMvc.perform(get("/api/inventory/pending"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{
                          "uuid": "%s",
                          "houseId": "%s",
                          "barcode": "0123456789012",
                          "expirationImage": "%s"
                        }]""".formatted(PENDING_ID, HOUSE_ID, expirationImageBase64), true));
    }

    @Test
    @DisplayName("should find a pending item by ID")
    void shouldFindPendingItemById() throws Exception {
        var pending = new PendingInventoryItem(PENDING_ID, null, HOUSE_ID, "0123456789012", null, measureImageBytes);

        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(get("/api/inventory/pending/{uuid}", PENDING_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "uuid": "%s",
                          "houseId": "%s",
                          "barcode": "0123456789012",
                          "measureImage": "%s"
                        }""".formatted(PENDING_ID, HOUSE_ID, measureImageBase64), true));
    }

    @Test
    @DisplayName("should 404 finding a pending item belonging to another house")
    void findByIdOtherHouse() throws Exception {
        var pending = new PendingInventoryItem(PENDING_ID, null, "other-house", null, null, null);
        when(houseAccess.belongsToHeaderHouse(any(PendingInventoryItem.class))).thenReturn(false);
        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(get("/api/inventory/pending/{uuid}", PENDING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should delete a pending item")
    void shouldDeletePendingItem() throws Exception {
        var pending = new PendingInventoryItem(PENDING_ID, null, HOUSE_ID, null, null, null);
        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(delete("/api/inventory/pending/{uuid}", PENDING_ID).with(csrf()))
                .andExpect(status().isNoContent());

        verify(pendingInventoryService).deleteById(PENDING_ID);
    }

    @Test
    @DisplayName("should 404 when deleting an unknown pending item")
    void deleteUnknown() throws Exception {
        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/inventory/pending/{uuid}", PENDING_ID).with(csrf()))
                .andExpect(status().isNotFound());

        verify(pendingInventoryService, never()).deleteById(any());
    }

    @Test
    @DisplayName("should 404 when deleting a pending item belonging to another house")
    void deleteOtherHouse() throws Exception {
        var pending = new PendingInventoryItem(PENDING_ID, null, "other-house", null, null, null);
        when(houseAccess.belongsToHeaderHouse(any(PendingInventoryItem.class))).thenReturn(false);
        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(delete("/api/inventory/pending/{uuid}", PENDING_ID).with(csrf()))
                .andExpect(status().isNotFound());

        verify(pendingInventoryService, never()).deleteById(any());
    }

    @Test
    @DisplayName("complete creates the inventory item and returns it")
    void shouldCompletePendingItem() throws Exception {
        var pending = new PendingInventoryItem(PENDING_ID, null, HOUSE_ID, "0123456789012", null, null);
        var item = new InventoryItem(null, null, HOUSE_ID, "bread", "item", 20d, LocalDate.of(2020, Month.JANUARY, 1), "0123456789012");
        var savedItem = item.withId(PENDING_ID);

        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));
        when(pendingInventoryService.complete(eq(PENDING_ID), any(InventoryItem.class))).thenReturn(savedItem);

        mockMvc.perform(post("/api/inventory/pending/{uuid}/complete", PENDING_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "bread",
                                  "measure": "item",
                                  "expiration": "2020-01-01",
                                  "amount": 20.0,
                                  "barcode": "0123456789012"
                                }"""))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "uuid": "%s",
                          "houseId": "%s",
                          "name": "bread",
                          "measure": "item",
                          "expiration": "2020-01-01",
                          "amount": 20.0,
                          "remaining": 20.0,
                          "barcode": "0123456789012"
                        }""".formatted(PENDING_ID, HOUSE_ID), true));

        verify(pendingInventoryService).complete(eq(PENDING_ID), any(InventoryItem.class));
    }

    @Test
    @DisplayName("complete 404s when the pending item is unknown")
    void completeUnknown() throws Exception {
        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/inventory/pending/{uuid}/complete", PENDING_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "bread",
                                  "measure": "item",
                                  "expiration": "2020-01-01",
                                  "amount": 20.0
                                }"""))
                .andExpect(status().isNotFound());

        verify(pendingInventoryService, never()).complete(any(), any());
    }

    @Test
    @DisplayName("complete 404s for a pending item belonging to another house")
    void completeOtherHouse() throws Exception {
        var pending = new PendingInventoryItem(PENDING_ID, null, "other-house", null, null, null);
        when(houseAccess.belongsToHeaderHouse(any(PendingInventoryItem.class))).thenReturn(false);
        when(pendingInventoryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(post("/api/inventory/pending/{uuid}/complete", PENDING_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "bread",
                                  "measure": "item",
                                  "expiration": "2020-01-01",
                                  "amount": 20.0
                                }"""))
                .andExpect(status().isNotFound());

        verify(pendingInventoryService, never()).complete(any(), any());
    }

    @Test
    @DisplayName("complete is forbidden for read-only members")
    @WithHouseMember
    void completeForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(post("/api/inventory/pending/{uuid}/complete", PENDING_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "bread",
                                  "measure": "item",
                                  "expiration": "2020-01-01",
                                  "amount": 20.0
                                }"""))
                .andExpect(status().isForbidden());
    }
}
