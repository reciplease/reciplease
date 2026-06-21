package org.reciplease.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithHouseMember;
import org.reciplease.configuration.WithHouseOwner;
import org.reciplease.configuration.WithMockRecipleaseUser;
import org.reciplease.dto.RecipeDto;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.service.RecipeService;
import org.reciplease.service.request.AddIngredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Imports {@link org.reciplease.configuration.MethodSecurityTestSupport} to exercise the same
 * {@code @HouseOwner} annotations the controller uses under real method security.
 */
@WebMvcTest(RecipeController.class)
@WithMockRecipleaseUser
@Import(MethodSecurityTestSupport.class)
class RecipeControllerTest {

    private static final String HOUSE_ID = "house-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean(name = "houseAccess")
    private HouseAccess houseAccess;

    @Test
    @DisplayName("ID does not exist")
    void noRecipe() throws Exception {
        final var id = UUID.randomUUID().toString();
        when(recipeService.findVisibleById(id, null)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recipes/{uuid}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get recipe by ID")
    void recipe() throws Exception {
        final var soup = getSoup();
        final var soupDto = RecipeDto.from(soup);

        when(recipeService.findVisibleById(soup.id(), null)).thenReturn(Optional.of(soup));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(soupDto), true));
    }

    @Test
    @DisplayName("get all recipes")
    void allRecipes() throws Exception {
        final var recipes = List.of(getToast(), getSoup());
        final var recipeDtoList = recipes.stream()
                .map(RecipeDto::from)
                .collect(toList());

        when(recipeService.findVisibleTo(null)).thenReturn(recipes);

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(recipeDtoList), true));
    }

    @Test
    @DisplayName("create new recipe")
    @WithHouseOwner
    void createRecipe() throws Exception {
        final var newSoupDto = getNewSoupDto();
        final var savedSoup = getSavedSoup();
        final var savedSoupDto = RecipeDto.from(savedSoup);

        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.requireHouseId()).thenReturn(HOUSE_ID);
        when(recipeService.create(HOUSE_ID, newSoupDto.toEntity())).thenReturn(savedSoup);

        final var json = mapper.writeValueAsString(newSoupDto);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(savedSoupDto), true));
    }

    @Test
    @DisplayName("create new recipe is forbidden for non-owners")
    @WithHouseMember
    void createRecipeForbiddenForNonOwner() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        final var json = mapper.writeValueAsString(getNewSoupDto());

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verify(recipeService, never()).create(any(), any());
    }

    @Test
    @WithHouseOwner
    void addRecipeIngredient() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .houseId(HOUSE_ID)
                .build();

        final var addIngredientRequest = new AddIngredient("tomato", "ITEMS", 10d);
        var savedRecipeIngredient = new RecipeIngredient("tomato", "ITEMS", 10d);

        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.belongsToHeaderHouse(recipe)).thenReturn(true);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));
        when(recipeService.addIngredient(recipe.id(), addIngredientRequest)).thenReturn(Set.of(savedRecipeIngredient));

        final var data = "{\"name\": \"tomato\", \"measure\": \"ITEMS\", \"amount\": 10.0}";
        // Response normalizes the legacy measure id to its short form.
        final var expectedJson = "[{\"name\": \"tomato\", \"measure\": \"item\", \"amount\": 10.0}]";

        mockMvc.perform(put("/api/recipes/{uuid}/ingredients", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    @DisplayName("update recipe when current user is an OWNER of the recipe's house")
    @WithHouseOwner
    void updateRecipeAsOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .houseId(HOUSE_ID)
                .build();
        final var updated = recipe.toBuilder().name("tomato soup").build();
        final var updateDto = RecipeDto.builder().recipeId(recipe.id()).name("tomato soup").build();

        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.belongsToHeaderHouse(recipe)).thenReturn(true);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));
        when(recipeService.update(recipe.id(), updateDto.toEntity())).thenReturn(updated);

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(RecipeDto.from(updated)), true));
    }

    @Test
    @DisplayName("update recipe is forbidden for non-owners")
    @WithHouseMember
    void updateRecipeForbiddenForNonOwner() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .houseId(HOUSE_ID)
                .build();
        final var updateDto = RecipeDto.builder().recipeId(recipe.id()).name("tomato soup").build();

        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        verify(recipeService, never()).update(any(), any());
    }

    @Test
    @DisplayName("update recipe is not found when it belongs to a different house than the header asserts")
    @WithHouseOwner
    void updateRecipeForbiddenForDifferentHouse() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .houseId("some-other-house")
                .build();
        final var updateDto = RecipeDto.builder().recipeId(recipe.id()).name("tomato soup").build();

        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.belongsToHeaderHouse(recipe)).thenReturn(false);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        mockMvc.perform(put("/api/recipes/{uuid}", recipe.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(recipeService, never()).update(any(), any());
    }

    @Test
    @DisplayName("delete recipe by ID")
    @WithHouseOwner
    void deleteRecipe() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .houseId(HOUSE_ID)
                .build();

        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.belongsToHeaderHouse(recipe)).thenReturn(true);
        when(recipeService.findById(recipe.id())).thenReturn(Optional.of(recipe));

        mockMvc.perform(delete("/api/recipes/{uuid}", recipe.id()))
                .andExpect(status().isNoContent());

        verify(recipeService).deleteById(recipe.id());
    }

    @Test
    @DisplayName("update recipe returns 404 when recipe does not exist")
    @WithHouseOwner
    void updateRecipeNotFound() throws Exception {
        final var id = UUID.randomUUID().toString();
        final var updateDto = RecipeDto.builder().recipeId(id).name("tomato soup").build();

        when(houseAccess.isOwner()).thenReturn(true);
        when(recipeService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/recipes/{uuid}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    private RecipeDto getNewSoupDto() {
        return RecipeDto.builder()
                .name("soup")
                .build();
    }

    private Recipe getSavedSoup() {
        return Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .houseId(HOUSE_ID)
                .build();
    }

    private Recipe getSoup() {
        return Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .build()
                .addIngredient("tomato", "ITEMS", 5d);
    }

    private Recipe getToast() {
        return Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("toast")
                .build()
                .addIngredient("bread", "ITEMS", 1d);
    }

}
