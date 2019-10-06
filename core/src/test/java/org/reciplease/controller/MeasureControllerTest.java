package org.reciplease.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reciplease.model.Measure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MeasureController.class)
public class MeasureControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnListOfMeasures() throws Exception {
        mockMvc.perform(get("/api/measures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(Measure.values().length)))
                .andExpect(jsonPath("$", containsInAnyOrder(
                        Arrays.stream(Measure.values()).map(Enum::toString).toArray()
                )));
    }
}