package org.reciplease.controller;

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

import java.time.LocalDate;
import java.time.Month;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithHouseMember;
import org.reciplease.configuration.WithHouseOwner;
import org.reciplease.model.PantryItem;
import org.reciplease.model.PendingPantryItem;
import org.reciplease.service.PantryService;
import org.reciplease.service.PendingPantryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// PantryController is loaded alongside so the tests prove the literal `pending` segment
// wins over its `api/pantry/{uuid}` mapping rather than being captured as a uuid.
@WebMvcTest({PendingPantryController.class, PantryController.class})
@WithHouseOwner
@Import(MethodSecurityTestSupport.class)
class PendingPantryControllerTest {

    private static final String HOUSE_ID = "house-1";
    private static final String PENDING_ID = "b465af6e-2465-4436-84c1-14f35db68dbf";

    @MockitoBean
    PendingPantryService pendingPantryService;

    @MockitoBean
    PantryService pantryService;

    @MockitoBean(name = "houseAccess")
    HouseAccess houseAccess;

    @Autowired
    private MockMvc mockMvc;

    private final byte[] barcodeImageBytes = new byte[] {7, 8, 9};
    private final byte[] expirationImageBytes = new byte[] {1, 2, 3};
    private final byte[] measureImageBytes = new byte[] {4, 5, 6};
    private final String barcodeImageBase64 = Base64.getEncoder().encodeToString(barcodeImageBytes);
    private final String expirationImageBase64 = Base64.getEncoder().encodeToString(expirationImageBytes);
    private final String measureImageBase64 = Base64.getEncoder().encodeToString(measureImageBytes);

    @BeforeEach
    void stubHouseAccess() {
        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.isMember()).thenReturn(true);
        when(houseAccess.requireHouseId()).thenReturn(HOUSE_ID);
        when(houseAccess.belongsToHeaderHouse(any(PendingPantryItem.class))).thenReturn(true);
    }

    @Test
    @DisplayName("should create a pending item with a barcode photo and both other images")
    void shouldCreatePendingItem() throws Exception {
        var requestItem =
                new PendingPantryItem(null, null, HOUSE_ID, barcodeImageBytes, expirationImageBytes, measureImageBytes);
        var savedItem = requestItem.withId(PENDING_ID);

        when(pendingPantryService.save(requestItem)).thenReturn(savedItem);

        mockMvc.perform(post("/api/pantry/pending")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "barcodeImage": "%s",
                                  "expirationImage": "%s",
                                  "measureImage": "%s"
                                }""".formatted(barcodeImageBase64, expirationImageBase64, measureImageBase64)))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .json(
                                """
                        {
                          "uuid": "%s",
                          "houseId": "%s",
                          "barcodeImage": "%s",
                          "expirationImage": "%s",
                          "measureImage": "%s"
                        }""".formatted(
                                                PENDING_ID,
                                                HOUSE_ID,
                                                barcodeImageBase64,
                                                expirationImageBase64,
                                                measureImageBase64),
                                true));
    }

    @Test
    @DisplayName("should create a pending item with every field skipped")
    void shouldCreateEmptyPendingItem() throws Exception {
        var requestItem = new PendingPantryItem(null, null, HOUSE_ID, null, null, null);
        var savedItem = requestItem.withId(PENDING_ID);

        when(pendingPantryService.save(requestItem)).thenReturn(savedItem);

        mockMvc.perform(post("/api/pantry/pending")
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
        var requestItem = new PendingPantryItem(null, null, HOUSE_ID, barcodeImageBytes, null, null);
        when(pendingPantryService.save(requestItem)).thenReturn(requestItem.withId(PENDING_ID));

        mockMvc.perform(post("/api/pantry/pending")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "uuid": "attacker-chosen-id",
                                  "barcodeImage": "%s"
                                }""".formatted(barcodeImageBase64)))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "uuid": "%s"
                        }""".formatted(PENDING_ID)));

        verify(pendingPantryService).save(requestItem);
    }

    @Test
    @DisplayName("create is forbidden for read-only members")
    @WithHouseMember
    void createForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(post("/api/pantry/pending")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should list the house's pending items")
    void shouldListPendingItems() throws Exception {
        var pending = new PendingPantryItem(PENDING_ID, null, HOUSE_ID, barcodeImageBytes, expirationImageBytes, null);

        when(pendingPantryService.findAll(HOUSE_ID)).thenReturn(List.of(pending));

        mockMvc.perform(get("/api/pantry/pending"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("""
                        [{
                          "uuid": "%s",
                          "houseId": "%s",
                          "barcodeImage": "%s",
                          "expirationImage": "%s"
                        }]""".formatted(PENDING_ID, HOUSE_ID, barcodeImageBase64, expirationImageBase64), true));
    }

    @Test
    @DisplayName("should find a pending item by ID")
    void shouldFindPendingItemById() throws Exception {
        var pending = new PendingPantryItem(PENDING_ID, null, HOUSE_ID, barcodeImageBytes, null, measureImageBytes);

        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(get("/api/pantry/pending/{uuid}", PENDING_ID))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("""
                        {
                          "uuid": "%s",
                          "houseId": "%s",
                          "barcodeImage": "%s",
                          "measureImage": "%s"
                        }""".formatted(PENDING_ID, HOUSE_ID, barcodeImageBase64, measureImageBase64), true));
    }

    @Test
    @DisplayName("should 404 finding a pending item belonging to another house")
    void findByIdOtherHouse() throws Exception {
        var pending = new PendingPantryItem(PENDING_ID, null, "other-house", null, null, null);
        when(houseAccess.belongsToHeaderHouse(any(PendingPantryItem.class))).thenReturn(false);
        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(get("/api/pantry/pending/{uuid}", PENDING_ID)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should delete a pending item")
    void shouldDeletePendingItem() throws Exception {
        var pending = new PendingPantryItem(PENDING_ID, null, HOUSE_ID, null, null, null);
        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(delete("/api/pantry/pending/{uuid}", PENDING_ID).with(csrf()))
                .andExpect(status().isNoContent());

        verify(pendingPantryService).deleteById(PENDING_ID);
    }

    @Test
    @DisplayName("should 404 when deleting an unknown pending item")
    void deleteUnknown() throws Exception {
        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/pantry/pending/{uuid}", PENDING_ID).with(csrf()))
                .andExpect(status().isNotFound());

        verify(pendingPantryService, never()).deleteById(any());
    }

    @Test
    @DisplayName("should 404 when deleting a pending item belonging to another house")
    void deleteOtherHouse() throws Exception {
        var pending = new PendingPantryItem(PENDING_ID, null, "other-house", null, null, null);
        when(houseAccess.belongsToHeaderHouse(any(PendingPantryItem.class))).thenReturn(false);
        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(delete("/api/pantry/pending/{uuid}", PENDING_ID).with(csrf()))
                .andExpect(status().isNotFound());

        verify(pendingPantryService, never()).deleteById(any());
    }

    @Test
    @DisplayName("complete creates the pantry item and returns it")
    void shouldCompletePendingItem() throws Exception {
        var pending = new PendingPantryItem(PENDING_ID, null, HOUSE_ID, barcodeImageBytes, null, null);
        var item = new PantryItem(
                null,
                null,
                HOUSE_ID,
                "bread",
                null,
                "item",
                20d,
                LocalDate.of(2020, Month.JANUARY, 1),
                "0123456789012");
        var savedItem = item.withId(PENDING_ID);

        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));
        when(pendingPantryService.complete(eq(PENDING_ID), any(PantryItem.class)))
                .thenReturn(savedItem);

        mockMvc.perform(post("/api/pantry/pending/{uuid}/complete", PENDING_ID)
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

        verify(pendingPantryService).complete(eq(PENDING_ID), any(PantryItem.class));
    }

    @Test
    @DisplayName("complete 404s when the pending item is unknown")
    void completeUnknown() throws Exception {
        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/pantry/pending/{uuid}/complete", PENDING_ID)
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

        verify(pendingPantryService, never()).complete(any(), any());
    }

    @Test
    @DisplayName("complete 404s for a pending item belonging to another house")
    void completeOtherHouse() throws Exception {
        var pending = new PendingPantryItem(PENDING_ID, null, "other-house", null, null, null);
        when(houseAccess.belongsToHeaderHouse(any(PendingPantryItem.class))).thenReturn(false);
        when(pendingPantryService.findById(PENDING_ID)).thenReturn(Optional.of(pending));

        mockMvc.perform(post("/api/pantry/pending/{uuid}/complete", PENDING_ID)
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

        verify(pendingPantryService, never()).complete(any(), any());
    }

    @Test
    @DisplayName("complete is forbidden for read-only members")
    @WithHouseMember
    void completeForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(post("/api/pantry/pending/{uuid}/complete", PENDING_ID)
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
