package org.reciplease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.dto.RecipeDto;
import org.reciplease.dto.RecipeIngredientDto;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeIngredient;
import org.reciplease.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
class RecipeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RecipeService recipeService;

    @Test
    @DisplayName("ID does not exist")
    void noRecipe() throws Exception {
        final var uuid = UUID.randomUUID();
        when(recipeService.findById(uuid)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recipes/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get recipe by ID")
    void recipe() throws Exception {
        final var soup = getSoup();
        final var soupDto = RecipeDto.from(soup);

        when(recipeService.findById(soup.getUuid())).thenReturn(Optional.of(soup));

        mockMvc.perform(get("/api/recipes/{uuid}", soup.getUuid()))
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
        final var ingredient = Ingredient.builder()
                .uuid(UUID.fromString("70991766-7944-40c2-be90-20065af3d02b"))
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();
        final var recipe = Recipe.builder()
                .randomUUID()
                .name("soup")
                .build();
        final var amount = 10d;

        final var recipeIngredient = new RecipeIngredient(ingredient.getUuid(), amount);
        final var savedRecipeIngredient = new RecipeIngredient(recipe, ingredient, amount);

        when(recipeService.addIngredient(recipe.getUuid(), recipeIngredient)).thenReturn(Set.of(savedRecipeIngredient));

        final var data = "{\"ingredientUuid\": \"70991766-7944-40c2-be90-20065af3d02b\", \"amount\": 10.0}";
        final var expectedJson = "[{\"ingredientUuid\": \"70991766-7944-40c2-be90-20065af3d02b\", \"name\": \"tomato\", \"measure\": \"ITEMS\", \"amount\": 10.0}]";

        mockMvc.perform(put("/api/recipes/{uuid}/ingredients", recipe.getUuid())
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
                .randomUUID()
                .name("soup")
                .build();
    }

    private Recipe getSoup() {
        return Recipe.builder()
                .randomUUID()
                .name("soup")
                .build()
                .addIngredient(getTomato(), 5d);
    }

    private Ingredient getTomato() {
        return Ingredient.builder()
                .randomUUID()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();
    }

    private Recipe getToast() {
        return Recipe.builder()
                .randomUUID()
                .name("toast")
                .build()
                .addIngredient(getBread(), 1d);
    }

    private Ingredient getBread() {
        return Ingredient.builder()
                .randomUUID()
                .name("bread")
                .measure(Measure.ITEMS)
                .build();
    }
}