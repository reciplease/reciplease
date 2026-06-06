package org.reciplease.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.dto.RecipeDto;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.service.RecipeService;
import org.reciplease.service.request.AddIngredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@WithMockUser
class RecipeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    @DisplayName("ID does not exist")
    void noRecipe() throws Exception {
        final var id = UUID.randomUUID().toString();
        when(recipeService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recipes/{uuid}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get recipe by ID")
    void recipe() throws Exception {
        final var soup = getSoup();
        final var soupDto = RecipeDto.from(soup);

        when(recipeService.findById(soup.getId())).thenReturn(Optional.of(soup));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.getId()))
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

        when(recipeService.findAll()).thenReturn(recipes);

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(recipeDtoList), true));
    }

    @Test
    @DisplayName("create new recipe")
    void createRecipe() throws Exception {
        final var newSoupDto = getNewSoupDto();
        final var savedSoup = getSavedSoup();
        final var savedSoupDto = RecipeDto.from(savedSoup);

        when(recipeService.create(newSoupDto.toEntity())).thenReturn(savedSoup);

        final var json = mapper.writeValueAsString(newSoupDto);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(savedSoupDto), true));
    }

    @Test
    void addRecipeIngredient() throws Exception {
        final var recipe = Recipe.builder()
                .id(UUID.randomUUID().toString())
                .name("soup")
                .build();

        final var addIngredientRequest = new AddIngredient("tomato", "ITEMS", 10d);
        final var savedRecipeIngredient = RecipeIngredient.builder()
                .name("tomato").measure("ITEMS").amount(10d).build();

        when(recipeService.addIngredient(recipe.getId(), addIngredientRequest)).thenReturn(Set.of(savedRecipeIngredient));

        final var data = "{\"name\": \"tomato\", \"measure\": \"ITEMS\", \"amount\": 10.0}";
        // Response normalizes the legacy measure id to its short form.
        final var expectedJson = "[{\"name\": \"tomato\", \"measure\": \"item\", \"amount\": 10.0}]";

        mockMvc.perform(put("/api/recipes/{uuid}/ingredients", recipe.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJson, true));
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
