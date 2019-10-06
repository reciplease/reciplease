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

    private static final String API_INGREDIENTS = "/api/ingredients";
    private static final String ID = "ABC";
    private static final String INGREDIENT_NAME = "ingredient name";
    private static final Measure MEASURE = Measure.GRAMS;

    @MockBean
    IngredientRepository ingredientRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void shouldCreateIngredient() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .name(INGREDIENT_NAME)
                .measure(Measure.KILOGRAMS)
                .build();

        when(ingredientRepository.save(any(Ingredient.class))).then(invocation -> invocation.<Ingredient>getArgument(0).toBuilder().id(ID).build());

        String json = mapper.writeValueAsString(ingredient);

        mockMvc.perform(post(API_INGREDIENTS)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ID)));
    }

    @Test
    public void shouldNotCreateIngredient_invalidJson() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .name("")
                .measure(null)
                .build();

        String json = mapper.writeValueAsString(ingredient);

        mockMvc.perform(post(API_INGREDIENTS)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetIngredientById() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .id(ID)
                .name(INGREDIENT_NAME)
                .measure(MEASURE)
                .build();

        when(ingredientRepository.findById(ID)).thenReturn(Optional.of(ingredient));

        mockMvc.perform(get(API_INGREDIENTS + "/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.name", is(INGREDIENT_NAME)))
                .andExpect(jsonPath("$.measure", is(MEASURE.toString())));
    }

    @Test
    public void shouldNotGetIngredient_notFound() throws Exception {
        when(ingredientRepository.findById(ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_INGREDIENTS + "/" + ID))
                .andExpect(status().isNotFound());
    }
}