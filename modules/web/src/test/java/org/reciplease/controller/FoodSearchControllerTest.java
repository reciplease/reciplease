package org.reciplease.controller;

import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.model.Nutrients;
import org.reciplease.service.FoodSearchService;
import org.reciplease.service.FoodSearchService.FoodSearchResult;
import org.reciplease.service.FoodSearchService.FoodSearchResultSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FoodSearchController.class)
@WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
@Import(MethodSecurityTestSupport.class)
class FoodSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FoodSearchService foodSearchService;

    @Test
    void searchReturnsMergedResults() throws Exception {
        when(foodSearchService.search("user-1", "chick")).thenReturn(List.of(
                new FoodSearchResult(FoodSearchResultSource.HISTORY, "Chicken Breast, grilled", "food-1", null),
                new FoodSearchResult(FoodSearchResultSource.CATALOG, "Chicken Tikka Masala", null,
                        new Nutrients(200.0, 15.0, 8.0, 10.0))));

        mockMvc.perform(get("/api/food/search").param("query", "chick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source", is("HISTORY")))
                .andExpect(jsonPath("$[0].displayName", is("Chicken Breast, grilled")))
                .andExpect(jsonPath("$[0].identifiedFoodId", is("food-1")))
                .andExpect(jsonPath("$[1].source", is("CATALOG")))
                .andExpect(jsonPath("$[1].nutrients.energyKcal", is(200.0)));
    }

    @Test
    void barcodeReturnsTheMatchWhenFound() throws Exception {
        when(foodSearchService.searchByBarcode("012345")).thenReturn(Optional.of(
                new FoodSearchResult(FoodSearchResultSource.CATALOG, "Oat Milk", null, new Nutrients(45.0, 1.0, 1.5, 6.0))));

        mockMvc.perform(get("/api/food/barcode/012345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("Oat Milk")))
                .andExpect(jsonPath("$.nutrients.proteinG", is(1.0)));
    }

    @Test
    void barcodeReturnsNotFoundWhenNoMatch() throws Exception {
        when(foodSearchService.searchByBarcode("000000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/food/barcode/000000"))
                .andExpect(status().isNotFound());
    }
}
