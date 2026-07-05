package org.reciplease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithHouseMember;
import org.reciplease.configuration.WithHouseOwner;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.PlannedMeal;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.model.ShoppingList;
import org.reciplease.service.PlannedMealService;
import org.reciplease.service.RecipeService;
import org.reciplease.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlannedMealController.class)
@WithHouseOwner
@Import(MethodSecurityTestSupport.class)
class PlannedMealControllerTest {

    private static final String HOUSE_ID = "house-1";

    @MockitoBean
    private PlannedMealService plannedMealService;

    @MockitoBean
    private ShoppingListService shoppingListService;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean(name = "houseAccess")
    HouseAccess houseAccess;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void stubHouseAccess() {
        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.isMember()).thenReturn(true);
        when(houseAccess.requireHouseId()).thenReturn(HOUSE_ID);
    }

    @Test
    @DisplayName("plans a meal linked to a recipe with inventory allocations")
    void plan() throws Exception {
        var recipe = Recipe.builder().id("recipe-1").name("toast").build();
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var planned = new PlannedMeal(HOUSE_ID, "recipe-1", "Dinner", LocalDate.of(2026, 6, 6),
                List.of(new PlannedIngredient(bread, List.of(new InventoryAllocation("item-1", "111", 2d)))));

        when(plannedMealService.plan(eq(HOUSE_ID), eq("recipe-1"), eq("Dinner"), eq(LocalDate.of(2026, 6, 6)), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(planned);
        when(recipeService.findById("recipe-1")).thenReturn(Optional.of(recipe));

        var body = "{"
                + "\"recipeId\":\"recipe-1\","
                + "\"name\":\"Dinner\","
                + "\"date\":\"2026-06-06\","
                + "\"items\":[{"
                + "\"ingredient\":{\"name\":\"bread\",\"measure\":\"ITEMS\",\"amount\":2.0},"
                + "\"allocations\":[{\"inventoryItemId\":\"item-1\",\"amount\":2.0}]"
                + "}]}";

        mockMvc.perform(post("/api/planned-meals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dinner"))
                .andExpect(jsonPath("$.recipe.recipeId").value("recipe-1"))
                .andExpect(jsonPath("$.items[0].allocations[0].barcode").value("111"));
    }

    @Test
    @DisplayName("plans a meal with just a name, no recipe or items")
    void planWithoutRecipeOrItems() throws Exception {
        var planned = new PlannedMeal(HOUSE_ID, null, "Leftover rice night", LocalDate.of(2026, 6, 6), List.of());

        when(plannedMealService.plan(HOUSE_ID, null, "Leftover rice night", LocalDate.of(2026, 6, 6), List.of()))
                .thenReturn(planned);

        var body = "{\"name\":\"Leftover rice night\",\"date\":\"2026-06-06\"}";

        mockMvc.perform(post("/api/planned-meals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Leftover rice night"))
                .andExpect(jsonPath("$.recipe").doesNotExist())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @DisplayName("rejects a plan request with no name")
    void planRequiresName() throws Exception {
        var body = "{\"date\":\"2026-06-06\"}";

        mockMvc.perform(post("/api/planned-meals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("plan is forbidden for read-only members")
    @WithHouseMember
    void planForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        var body = "{\"name\":\"Dinner\",\"date\":\"2026-06-06\"}";

        mockMvc.perform(post("/api/planned-meals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("finds a planned meal owned by the caller's house by id")
    void findByIdReturnsMeal() throws Exception {
        var meal = new PlannedMeal("meal-1", null, HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(meal));
        when(houseAccess.belongsToHeaderHouse(meal)).thenReturn(true);

        mockMvc.perform(get("/api/planned-meals/{uuid}", "meal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dinner"));
    }

    @Test
    @DisplayName("find by id is not found when the meal doesn't exist")
    void findByIdNotFound() throws Exception {
        when(plannedMealService.findById("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/planned-meals/{uuid}", "ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("find by id is not found when the meal belongs to a different house than the header asserts")
    void findByIdForbiddenForDifferentHouse() throws Exception {
        var meal = new PlannedMeal("meal-1", null, "some-other-house", null, "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(meal));
        when(houseAccess.belongsToHeaderHouse(meal)).thenReturn(false);

        mockMvc.perform(get("/api/planned-meals/{uuid}", "meal-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("updates a planned meal owned by the caller's house")
    void update() throws Exception {
        var existing = new PlannedMeal("meal-1", null, HOUSE_ID, "recipe-1", "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);
        var updated = new PlannedMeal("meal-1", null, HOUSE_ID, "recipe-1", "Supper", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(existing));
        when(houseAccess.belongsToHeaderHouse(existing)).thenReturn(true);
        when(plannedMealService.update(eq("meal-1"), eq("recipe-1"), eq("Supper"), eq(LocalDate.of(2026, 6, 6)), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(updated);

        var body = "{\"recipeId\":\"recipe-1\",\"name\":\"Supper\",\"date\":\"2026-06-06\"}";

        mockMvc.perform(put("/api/planned-meals/{uuid}", "meal-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Supper"));
    }

    @Test
    @DisplayName("update is not found when the meal doesn't exist")
    void updateNotFound() throws Exception {
        when(plannedMealService.findById("ghost")).thenReturn(Optional.empty());

        var body = "{\"name\":\"Supper\",\"date\":\"2026-06-06\"}";

        mockMvc.perform(put("/api/planned-meals/{uuid}", "ghost")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        verify(plannedMealService, never()).update(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("update is not found when the meal belongs to a different house than the header asserts")
    void updateForbiddenForDifferentHouse() throws Exception {
        var existing = new PlannedMeal("meal-1", null, "some-other-house", null, "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(existing));
        when(houseAccess.belongsToHeaderHouse(existing)).thenReturn(false);

        var body = "{\"name\":\"Supper\",\"date\":\"2026-06-06\"}";

        mockMvc.perform(put("/api/planned-meals/{uuid}", "meal-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        verify(plannedMealService, never()).update(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("update is forbidden for read-only members")
    @WithHouseMember
    void updateForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        var body = "{\"name\":\"Supper\",\"date\":\"2026-06-06\"}";

        mockMvc.perform(put("/api/planned-meals/{uuid}", "meal-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deletes a planned meal owned by the caller's house")
    void deleteMeal() throws Exception {
        var existing = new PlannedMeal("meal-1", null, HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(existing));
        when(houseAccess.belongsToHeaderHouse(existing)).thenReturn(true);

        mockMvc.perform(delete("/api/planned-meals/{uuid}", "meal-1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(plannedMealService).deleteById("meal-1");
    }

    @Test
    @DisplayName("delete is not found when the meal doesn't exist")
    void deleteNotFound() throws Exception {
        when(plannedMealService.findById("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/planned-meals/{uuid}", "ghost").with(csrf()))
                .andExpect(status().isNotFound());

        verify(plannedMealService, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete is not found when the meal belongs to a different house than the header asserts")
    void deleteForbiddenForDifferentHouse() throws Exception {
        var existing = new PlannedMeal("meal-1", null, "some-other-house", null, "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(existing));
        when(houseAccess.belongsToHeaderHouse(existing)).thenReturn(false);

        mockMvc.perform(delete("/api/planned-meals/{uuid}", "meal-1").with(csrf()))
                .andExpect(status().isNotFound());

        verify(plannedMealService, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete is forbidden for read-only members")
    @WithHouseMember
    void deleteForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(delete("/api/planned-meals/{uuid}", "meal-1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("marks a planned meal owned by the caller's house as eaten")
    void markEaten() throws Exception {
        var existing = new PlannedMeal("meal-1", null, HOUSE_ID, null, "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(existing));
        when(houseAccess.belongsToHeaderHouse(existing)).thenReturn(true);
        when(plannedMealService.markEaten("meal-1")).thenReturn(existing);

        mockMvc.perform(post("/api/planned-meals/{uuid}/eaten", "meal-1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dinner"));
    }

    @Test
    @DisplayName("mark eaten is not found when the meal doesn't exist")
    void markEatenNotFound() throws Exception {
        when(plannedMealService.findById("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/planned-meals/{uuid}/eaten", "ghost").with(csrf()))
                .andExpect(status().isNotFound());

        verify(plannedMealService, never()).markEaten(any());
    }

    @Test
    @DisplayName("mark eaten is not found when the meal belongs to a different house than the header asserts")
    void markEatenForbiddenForDifferentHouse() throws Exception {
        var existing = new PlannedMeal("meal-1", null, "some-other-house", null, "Dinner", LocalDate.of(2026, 6, 6), List.of(), null, null, null);

        when(plannedMealService.findById("meal-1")).thenReturn(Optional.of(existing));
        when(houseAccess.belongsToHeaderHouse(existing)).thenReturn(false);

        mockMvc.perform(post("/api/planned-meals/{uuid}/eaten", "meal-1").with(csrf()))
                .andExpect(status().isNotFound());

        verify(plannedMealService, never()).markEaten(any());
    }

    @Test
    @DisplayName("mark eaten is forbidden for read-only members")
    @WithHouseMember
    void markEatenForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(post("/api/planned-meals/{uuid}/eaten", "meal-1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns inventory suggestions for an ingredient, scoped to a recipe")
    void suggestionsForRecipe() throws Exception {
        var item = new InventoryItem("item-1", null, HOUSE_ID, "bread", "ITEMS", 5d, LocalDate.of(2026, 6, 30), "111");

        when(plannedMealService.suggestInventory(HOUSE_ID, "recipe-1", "bread")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/planned-meals/suggestions")
                        .param("recipeId", "recipe-1")
                        .param("ingredient", "bread"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"uuid\":\"item-1\",\"houseId\":\"house-1\",\"name\":\"bread\",\"measure\":\"item\",\"amount\":5.0,\"remaining\":5.0,\"expiration\":\"2026-06-30\",\"barcode\":\"111\"}]", true));
    }

    @Test
    @DisplayName("returns inventory suggestions for an ingredient with no recipe given")
    void suggestionsWithoutRecipe() throws Exception {
        var item = new InventoryItem("item-1", null, HOUSE_ID, "bread", "ITEMS", 5d, LocalDate.of(2026, 6, 30), "111");

        when(plannedMealService.suggestInventory(HOUSE_ID, null, "bread")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/planned-meals/suggestions")
                        .param("ingredient", "bread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").value("item-1"));
    }

    @Test
    @DisplayName("lists planned meals in a date range")
    void findByDateRange() throws Exception {
        var planned = new PlannedMeal(HOUSE_ID, null, "Leftover rice night", LocalDate.of(2026, 6, 6), List.of());

        when(plannedMealService.findByDateIsBetween(HOUSE_ID, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(planned));

        mockMvc.perform(get("/api/planned-meals")
                        .param("start", "2026-06-01")
                        .param("end", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Leftover rice night"));
    }

    @Test
    @DisplayName("returns the shopping list gap for a date range")
    void shoppingList() throws Exception {
        var shoppingList = new ShoppingList(List.of(new RecipeIngredient("bread", "item", 4d)));

        when(shoppingListService.generateShoppingList(HOUSE_ID, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)))
                .thenReturn(shoppingList);

        mockMvc.perform(get("/api/planned-meals/shopping-list")
                        .param("start", "2026-06-01")
                        .param("end", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("bread"))
                .andExpect(jsonPath("$.items[0].amount").value(4.0));
    }
}
