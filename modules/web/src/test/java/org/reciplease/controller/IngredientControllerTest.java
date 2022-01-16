package org.reciplease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.reciplease.dto.IngredientDto;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.reciplease.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngredientController.class)
public class IngredientControllerTest {

    private static final String API_INGREDIENTS = "/api/ingredients";
    private static final UUID ID = UUID.randomUUID();
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

        when(ingredientRepository.save(any(Ingredient.class))).then(invocation -> invocation.getArgument(0, Ingredient.class).toBuilder().uuid(ID).build());

        final String json = mapper.writeValueAsString(ingredient);

        mockMvc.perform(post(API_INGREDIENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid", is(ID.toString())));
    }

    @Test
    public void shouldNotCreateIngredient_invalidJson() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .name("")
                .measure(null)
                .build();

        final String json = mapper.writeValueAsString(ingredient);

        mockMvc.perform(post(API_INGREDIENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetIngredientById() throws Exception {
        final Ingredient ingredient = Ingredient.builder()
                .uuid(ID)
                .name(INGREDIENT_NAME)
                .measure(MEASURE)
                .build();

        when(ingredientRepository.findByUuid(ID)).thenReturn(Optional.of(ingredient));

        mockMvc.perform(get(API_INGREDIENTS + "/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(ID.toString())))
                .andExpect(jsonPath("$.name", is(INGREDIENT_NAME)))
                .andExpect(jsonPath("$.measure", is(MEASURE.toString())));
    }

    @Test
    public void shouldNotGetIngredient_notFound() throws Exception {
        when(ingredientRepository.findByUuid(ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(API_INGREDIENTS + "/" + ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetAllIngredients() throws Exception {
        final var ingredientDto = IngredientDto.builder()
                .uuid(UUID.randomUUID())
                .name(INGREDIENT_NAME)
                .measure(MEASURE)
                .build();
        final var ingredientDto2 = IngredientDto.builder()
                .uuid(UUID.randomUUID())
                .name(INGREDIENT_NAME + "2")
                .measure(MEASURE)
                .build();
        final var expected = List.of(ingredientDto, ingredientDto2);
        final var mockResult = List.of(ingredientDto.toModel(), ingredientDto2.toModel());
        when(ingredientRepository.findAll()).thenReturn(mockResult);

        final var result = mockMvc.perform(get(API_INGREDIENTS))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var actual = List.of(mapper.readValue(result, IngredientDto[].class));

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldSearch() throws Exception {
        final var ingredientDto = IngredientDto.builder()
                .uuid(ID)
                .name(INGREDIENT_NAME)
                .measure(MEASURE)
                .build();
        final var expected = List.of(ingredientDto);
        final var mockResult = List.of(ingredientDto.toModel());
        when(ingredientRepository.searchByName("abc")).thenReturn(mockResult);

        final var result = mockMvc.perform(get(API_INGREDIENTS + "/search/abc"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final var actual = List.of(mapper.readValue(result, IngredientDto[].class));

        assertThat(actual, equalTo(expected));
    }
}
