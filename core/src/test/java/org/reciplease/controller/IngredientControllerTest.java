package org.reciplease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.reciplease.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(IngredientController.class)
public class IngredientControllerTest {
    @MockBean
    IngredientRepository ingredientRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void shouldCreateIngredient() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .name("ingredient name")
                .measure(Measure.KILOGRAMS)
                .build();

        when(ingredientRepository.save(any(Ingredient.class))).then(invocation -> {
            final Ingredient ingredientArg = invocation.getArgument(0);
            return Ingredient.builder()
                    .id("ABC")
                    .name(ingredientArg.getName())
                    .measure(ingredientArg.getMeasure())
                    .build();
        });

        String json = mapper.writeValueAsString(ingredient);

        mockMvc.perform(post("/api/ingredients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("ABC")));
    }

    @Test
    public void shouldNotCreateIngredient_invalidJson() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .name("")
                .measure(null)
                .build();

        String json = mapper.writeValueAsString(ingredient);

        mockMvc.perform(post("/api/ingredients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetIngredientById() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .id("ABC")
                .name("ingredient name")
                .measure(Measure.GRAMS)
                .build();

        when(ingredientRepository.findById("ABC")).thenReturn(Optional.of(ingredient));

        mockMvc.perform(get("/api/ingredients/ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ABC")))
                .andExpect(jsonPath("$.name", is("ingredient name")))
                .andExpect(jsonPath("$.measure", is(Measure.GRAMS.toString())));
    }
}