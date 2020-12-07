package org.reciplease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.reciplease.model.Recipe;
import org.reciplease.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
        when(recipeService.get(uuid)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recipes/{uuid}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("get recipe by ID")
    void recipeDto() throws Exception {
        final var ingredient = Ingredient.builder()
                .randomUUID()
                .name("tomato")
                .measure(Measure.ITEMS)
                .build();
        final var recipe = Recipe.builder()
                .randomUUID()
                .build()
                .addIngredient(ingredient, 5d);

        when(recipeService.get(recipe.getUuid())).thenReturn(Optional.of(recipe));

        mockMvc.perform(get("/api/recipes/{uuid}", recipe.getUuid()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(recipe)))
                .andDo(print());
    }
}