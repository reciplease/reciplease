package org.reciplease.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Measure;
import org.reciplease.repository.MeasureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeasureController.class)
@WithMockUser
public class MeasureControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

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

    @Test
    public void shouldCreateMeasure() throws Exception {
        final var created = Measure.builder().measureId("abc123").singular("litre").plural("litres").build();
        when(measureRepository.save(any(Measure.class))).thenReturn(created);

        final String body = mapper.writeValueAsString(Map.of("singular", "litre", "plural", "litres"));

        mockMvc.perform(post("/api/measures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.measureId", is("abc123")))
                .andExpect(jsonPath("$.singular", is("litre")))
                .andExpect(jsonPath("$.plural", is("litres")));
    }

    @Test
    public void shouldRejectCreateMeasureWithBlankSingular() throws Exception {
        final String body = mapper.writeValueAsString(Map.of("singular", "", "plural", "litres"));

        mockMvc.perform(post("/api/measures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectCreateMeasureWithBlankPlural() throws Exception {
        final String body = mapper.writeValueAsString(Map.of("singular", "litre", "plural", ""));

        mockMvc.perform(post("/api/measures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
