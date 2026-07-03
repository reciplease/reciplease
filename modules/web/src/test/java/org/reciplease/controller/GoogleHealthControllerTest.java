package org.reciplease.controller;

import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.model.GoogleHealthConnection;
import org.reciplease.service.GoogleHealthNotConnectedException;
import org.reciplease.service.GoogleHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoogleHealthController.class)
@WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
@Import(MethodSecurityTestSupport.class)
class GoogleHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoogleHealthService googleHealthService;

    @Test
    void connectionReportsConnectedWithExpiresAtAndRefreshTokenWhenALinkExists() throws Exception {
        final var expiresAt = Instant.parse("2026-07-02T13:00:00Z");
        when(googleHealthService.connectionStatus("user-1")).thenReturn(Optional.of(
                new GoogleHealthConnection("user-1", "access", "refresh", expiresAt, "nutrition.readonly", Instant.now(), Instant.now())));

        mockMvc.perform(get("/api/google-health/connection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected", is(true)))
                .andExpect(jsonPath("$.expiresAt", is(expiresAt.toString())))
                .andExpect(jsonPath("$.refreshToken", is("refresh")));
    }

    @Test
    void connectionReportsDisconnectedWhenNoLinkExists() throws Exception {
        when(googleHealthService.connectionStatus("user-1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/google-health/connection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected", is(false)))
                .andExpect(jsonPath("$.expiresAt").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void putConnectionStoresTheTokensAndReturnsConnected() throws Exception {
        final var expiresAt = Instant.parse("2026-07-02T13:00:00Z");
        when(googleHealthService.storeConnection("user-1", "access-1", "refresh-1", 3600, "nutrition.writeonly"))
                .thenReturn(new GoogleHealthConnection("user-1", "access-1", "refresh-1", expiresAt, "nutrition.writeonly", Instant.now(), Instant.now()));

        mockMvc.perform(put("/api/google-health/connection")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accessToken": "access-1", "refreshToken": "refresh-1", "expiresIn": 3600, "scope": "nutrition.writeonly"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected", is(true)))
                .andExpect(jsonPath("$.expiresAt", is(expiresAt.toString())))
                .andExpect(jsonPath("$.refreshToken", is("refresh-1")));

        verify(googleHealthService).storeConnection("user-1", "access-1", "refresh-1", 3600, "nutrition.writeonly");
    }

    @Test
    void disconnectDeletesTheConnection() throws Exception {
        mockMvc.perform(delete("/api/google-health/connection").with(csrf()))
                .andExpect(status().isNoContent());

        verify(googleHealthService).disconnect("user-1");
    }

    @Test
    void searchFoodsProxiesGoogleHealthsRawJsonResponse() throws Exception {
        when(googleHealthService.searchFoods("user-1", "banana")).thenReturn("{\"dataPoint\":[{\"foodId\":\"1\",\"name\":\"Banana\"}]}");

        mockMvc.perform(get("/api/google-health/foods/search").param("query", "banana"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"dataPoint\":[{\"foodId\":\"1\",\"name\":\"Banana\"}]}", true));
    }

    @Test
    void searchFoodsReturnsNotFoundWhenNoGoogleHealthAccountIsLinked() throws Exception {
        when(googleHealthService.searchFoods("user-1", "banana")).thenThrow(new GoogleHealthNotConnectedException("user-1"));

        mockMvc.perform(get("/api/google-health/foods/search").param("query", "banana"))
                .andExpect(status().isNotFound());
    }

    @Test
    void logFoodDelegatesToTheService() throws Exception {
        mockMvc.perform(post("/api/google-health/foods/log")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId": "food-1", "foodDisplayName": "Banana", "mealType": "LUNCH", "amount": 2.5, "date": "2026-07-02"}"""))
                .andExpect(status().isOk());

        verify(googleHealthService).logFood("user-1", "food-1", "Banana", "LUNCH", 2.5, LocalDate.of(2026, 7, 2));
    }

    @Test
    void logFoodReturnsNotFoundWhenNoGoogleHealthAccountIsLinked() throws Exception {
        doThrow(new GoogleHealthNotConnectedException("user-1"))
                .when(googleHealthService).logFood(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyDouble(), any());

        mockMvc.perform(post("/api/google-health/foods/log")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId": "food-1", "foodDisplayName": "Banana", "mealType": "LUNCH", "amount": 2.5, "date": "2026-07-02"}"""))
                .andExpect(status().isNotFound());
    }
}
