package org.reciplease.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.IngredientPairing;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PlannedRecipe;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.service.PlannedRecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlannedRecipeController.class)
@WithMockUser
class PlannedRecipeControllerTest {

    @MockitoBean
    private PlannedRecipeService plannedRecipeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("plans a recipe with inventory pairings")
    void plan() throws Exception {
        final var recipe = Recipe.builder().id("recipe-1").name("toast").build();
        final var bread = RecipeIngredient.builder().name("bread").measure("ITEMS").amount(2d).build();
        final var planned = PlannedRecipe.builder()
                .recipe(recipe)
                .date(LocalDate.of(2026, 6, 6))
                .pairing(IngredientPairing.builder()
                        .recipeIngredient(bread)
                        .allocation(InventoryAllocation.builder()
                                .inventoryItemId("item-1").barcode("111").amount(2d).build())
                        .build())
                .build();

        when(plannedRecipeService.plan(eq("recipe-1"), eq(LocalDate.of(2026, 6, 6)), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(planned);

        final var body = "{"
                + "\"recipeId\":\"recipe-1\","
                + "\"date\":\"2026-06-06\","
                + "\"pairings\":[{"
                + "\"recipeIngredient\":{\"name\":\"bread\",\"measure\":\"ITEMS\",\"amount\":2.0},"
                + "\"allocations\":[{\"inventoryItemId\":\"item-1\",\"amount\":2.0}]"
                + "}]}";

        mockMvc.perform(post("/api/planned-recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipe.recipeId").value("recipe-1"))
                .andExpect(jsonPath("$.pairings[0].allocations[0].barcode").value("111"));
    }

    @Test
    @DisplayName("returns inventory suggestions for a recipe ingredient")
    void suggestions() throws Exception {
        final var item = InventoryItem.builder()
                .id("item-1").name("bread").measure("ITEMS").amount(5d)
                .expiration(LocalDate.of(2026, 6, 30)).barcode("111").build();

        when(plannedRecipeService.suggestInventory("recipe-1", "bread")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/planned-recipes/{recipeId}/suggestions", "recipe-1")
                        .param("ingredient", "bread"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"uuid\":\"item-1\",\"name\":\"bread\",\"measure\":\"item\",\"amount\":5.0,\"expiration\":\"2026-06-30\",\"barcode\":\"111\"}]", true));
    }
}
