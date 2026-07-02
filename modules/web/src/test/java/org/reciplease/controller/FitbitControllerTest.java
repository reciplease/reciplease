package org.reciplease.controller;

import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.model.FitbitConnection;
import org.reciplease.service.FitbitNotConnectedException;
import org.reciplease.service.FitbitService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FitbitController.class)
@WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
@Import(MethodSecurityTestSupport.class)
class FitbitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FitbitService fitbitService;

    @Test
    void connectionReportsConnectedWhenALinkExists() throws Exception {
        when(fitbitService.connectionStatus("user-1")).thenReturn(Optional.of(
                new FitbitConnection("user-1", "access", "refresh", Instant.now(), "fitbit-user-1", Instant.now(), Instant.now())));

        mockMvc.perform(get("/api/fitbit/connection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected", is(true)));
    }

    @Test
    void connectionReportsDisconnectedWhenNoLinkExists() throws Exception {
        when(fitbitService.connectionStatus("user-1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/fitbit/connection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected", is(false)));
    }

    @Test
    void callbackExchangesTheCodeAndReturnsConnected() throws Exception {
        mockMvc.perform(post("/api/fitbit/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "auth-code-1", "codeVerifier": "verifier-1"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected", is(true)));

        verify(fitbitService).exchangeCode("user-1", "auth-code-1", "verifier-1");
    }

    @Test
    void disconnectDeletesTheConnection() throws Exception {
        mockMvc.perform(delete("/api/fitbit/connection").with(csrf()))
                .andExpect(status().isNoContent());

        verify(fitbitService).disconnect("user-1");
    }

    @Test
    void searchFoodsProxiesFitbitsRawJsonResponse() throws Exception {
        when(fitbitService.searchFoods("user-1", "banana")).thenReturn("{\"foods\":[{\"foodId\":1,\"name\":\"Banana\"}]}");

        mockMvc.perform(get("/api/fitbit/foods/search").param("query", "banana"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"foods\":[{\"foodId\":1,\"name\":\"Banana\"}]}", true));
    }

    @Test
    void searchFoodsReturnsNotFoundWhenNoFitbitAccountIsLinked() throws Exception {
        when(fitbitService.searchFoods("user-1", "banana")).thenThrow(new FitbitNotConnectedException("user-1"));

        mockMvc.perform(get("/api/fitbit/foods/search").param("query", "banana"))
                .andExpect(status().isNotFound());
    }

    @Test
    void logFoodDelegatesToTheService() throws Exception {
        mockMvc.perform(post("/api/fitbit/foods/log")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId": "food-1", "unitId": "147", "amount": 2.5, "mealTypeId": "1", "date": "2026-07-02"}"""))
                .andExpect(status().isOk());

        verify(fitbitService).logFood("user-1", "food-1", "147", 2.5, "1", LocalDate.of(2026, 7, 2));
    }

    @Test
    void logFoodReturnsNotFoundWhenNoFitbitAccountIsLinked() throws Exception {
        doThrow(new FitbitNotConnectedException("user-1"))
                .when(fitbitService).logFood(any(), any(), any(), org.mockito.ArgumentMatchers.anyDouble(), any(), any());

        mockMvc.perform(post("/api/fitbit/foods/log")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId": "food-1", "unitId": "147", "amount": 2.5, "mealTypeId": "1", "date": "2026-07-02"}"""))
                .andExpect(status().isNotFound());
    }
}
