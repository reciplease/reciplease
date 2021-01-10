package org.reciplease.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LocalSecurityConfig.class)
@ActiveProfiles("local")
public class LocalSecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldNotRequireLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().isNotFound());
    }
}

