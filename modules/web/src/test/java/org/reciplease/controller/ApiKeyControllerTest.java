package org.reciplease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithHouseMember;
import org.reciplease.configuration.WithHouseOwner;
import org.reciplease.model.ApiKey;
import org.reciplease.model.CreatedApiKey;
import org.reciplease.model.HouseRole;
import org.reciplease.repository.HouseRepository;
import org.reciplease.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiKeyController.class)
@WithHouseOwner
@Import(MethodSecurityTestSupport.class)
class ApiKeyControllerTest {

    private static final String HOUSE_ID = "house-1";

    @MockitoBean
    private ApiKeyService apiKeyService;
    @MockitoBean(name = "houseAccess")
    private HouseAccess houseAccess;
    @MockitoBean
    private HouseRepository houseRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void stubHouseAccess() {
        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.isMember()).thenReturn(true);
        when(houseAccess.requireHouseId()).thenReturn(HOUSE_ID);
    }

    @Test
    @DisplayName("should list keys for the house without exposing the raw secret")
    void findAll() throws Exception {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(apiKeyService.list(HOUSE_ID)).thenReturn(List.of(
                new ApiKey("key-1", HOUSE_ID, "Home Assistant", HouseRole.READ_ONLY, "owner-1", "rcpl_abc", "hash", createdAt, null)));

        mockMvc.perform(get("/api/houses/api-keys"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"id": "key-1", "name": "Home Assistant", "role": "READ_ONLY", "keyPrefix": "rcpl_abc", "createdAt": "2026-01-01T00:00:00Z"}
                        ]""", true));
    }

    @Test
    @DisplayName("findAll is forbidden for read-only members")
    @WithHouseMember
    void findAllForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(get("/api/houses/api-keys"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should create a key and return the raw secret once")
    @WithMockUser(username = "owner-1", authorities = "ROLE_RECIPLEASE")
    void create() throws Exception {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var apiKey = new ApiKey("key-1", HOUSE_ID, "Home Assistant", HouseRole.READ_ONLY, "owner-1", "rcpl_abc", "hash", createdAt, null);
        when(apiKeyService.create(HOUSE_ID, "Home Assistant", HouseRole.READ_ONLY, "owner-1"))
                .thenReturn(new CreatedApiKey(apiKey, "rcpl_abcdefghij1234567890"));

        mockMvc.perform(post("/api/houses/api-keys")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Home Assistant", "role": "READ_ONLY"}"""))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {"id": "key-1", "name": "Home Assistant", "role": "READ_ONLY",
                         "rawKey": "rcpl_abcdefghij1234567890", "createdAt": "2026-01-01T00:00:00Z"}""", true));
    }

    @Test
    @DisplayName("create is forbidden for read-only members")
    @WithHouseMember
    void createForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(post("/api/houses/api-keys")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Home Assistant", "role": "READ_ONLY"}"""))
                .andExpect(status().isForbidden());

        verify(apiKeyService, never()).create(any(), any(), any(), any());
    }

    @Test
    @DisplayName("should revoke a key")
    void revoke() throws Exception {
        when(apiKeyService.revoke(HOUSE_ID, "key-1")).thenReturn(true);

        mockMvc.perform(delete("/api/houses/api-keys/{id}", "key-1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should 404 when revoking an unknown or mismatched key")
    void revokeNotFound() throws Exception {
        when(apiKeyService.revoke(HOUSE_ID, "key-1")).thenReturn(false);

        mockMvc.perform(delete("/api/houses/api-keys/{id}", "key-1").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("revoke is forbidden for read-only members")
    @WithHouseMember
    void revokeForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(delete("/api/houses/api-keys/{id}", "key-1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
