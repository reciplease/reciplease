package org.reciplease.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @MockBean
    InventoryRepository inventoryRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldCreateInventoryItem() throws Exception {
        final UUID ingredientId = UUID.randomUUID();
        final UUID itemId = UUID.randomUUID();

        final Ingredient ingredient = Ingredient.builder()
                .uuid(ingredientId)
                .build();

        final InventoryItem item = InventoryItem.builder()
                .ingredient(ingredient)
                .amount(20d)
                .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                .build();

        when(inventoryRepository.save(any(InventoryItem.class))).then(invocation -> invocation.getArgument(0, InventoryItem.class).toBuilder().uuid(itemId).build());

        final String json = mapper.writeValueAsString(item);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid", is(itemId.toString())));
    }
}