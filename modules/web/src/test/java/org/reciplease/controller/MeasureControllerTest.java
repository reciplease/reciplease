package org.reciplease.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.reciplease.model.Measure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeasureController.class)
@WithMockUser
public class MeasureControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnAllMeasuresFromTheEnum() throws Exception {
        mockMvc.perform(get("/api/measures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(Measure.values().length)))
                .andExpect(jsonPath("$[*].measureId", hasItems("g", "kg", "ml", "cl", "l")));
    }

    @Test
    public void shouldReturnMeasureByShortId() throws Exception {
        mockMvc.perform(get("/api/measures/g"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measureId", is("g")))
                .andExpect(jsonPath("$.singular", is("gram")))
                .andExpect(jsonPath("$.plural", is("grams")))
                .andExpect(jsonPath("$.short", is("g")));
    }

    @Test
    public void shouldResolveLegacyMeasureId() throws Exception {
        mockMvc.perform(get("/api/measures/GRAMS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measureId", is("g")));
    }

    @Test
    public void shouldReturnNotFoundForUnknownMeasure() throws Exception {
        mockMvc.perform(get("/api/measures/SPOONFULS")).andExpect(status().isNotFound());
    }
}
