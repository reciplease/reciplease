package org.reciplease.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebAppConfiguration
@Import(WebSecurityConfig.class)
@ActiveProfiles("test")
public class WebSecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").accept(MediaType.TEXT_HTML))
                .build();
    }

    @Test
    public void shouldNotRequireSSL() throws Exception {
        mvc.perform(get("/")).andExpect(status().isNotFound());
    }

    @Test
    public void shouldRequireSSL_redirect() throws Exception {
        mvc.perform(get("/").header("X-Forwarded-Proto", true)).andExpect(status().is3xxRedirection());
    }

    @Test
    public void shouldRequireSSL() throws Exception {
        mvc.perform(get("/").header("X-Forwarded-Proto", true).secure(true)).andExpect(status().isNotFound());
    }
}

