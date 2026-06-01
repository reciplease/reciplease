package org.reciplease.controller;

import org.junit.jupiter.api.Test;
import org.reciplease.model.Measure;
import org.reciplease.repository.MeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeasureController.class)
@WithMockUser
public class MeasureControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeasureRepository measureRepository;

    @Test
    public void shouldReturnListOfMeasures() throws Exception {
        final var measures = List.of(
                Measure.builder().measureId("ITEMS").singular("item").plural("items").build(),
                Measure.builder().measureId("GRAMS").singular("gram").plural("grams").build()
        );
        when(measureRepository.findAll()).thenReturn(measures);

        mockMvc.perform(get("/api/measures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(measures.size())))
                .andExpect(jsonPath("$[*].measureId", containsInAnyOrder("ITEMS", "GRAMS")));
    }

    @Test
    public void shouldReturnMeasureById() throws Exception {
        final var grams = Measure.builder().measureId("GRAMS").singular("gram").plural("grams").build();
        when(measureRepository.findById("GRAMS")).thenReturn(Optional.of(grams));

        mockMvc.perform(get("/api/measures/GRAMS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measureId", is("GRAMS")))
                .andExpect(jsonPath("$.singular", is("gram")))
                .andExpect(jsonPath("$.plural", is("grams")));
    }

    @Test
    public void shouldReturnNotFoundForUnknownMeasure() throws Exception {
        when(measureRepository.findById("SPOONFULS")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/measures/SPOONFULS"))
                .andExpect(status().isNotFound());
    }
}
