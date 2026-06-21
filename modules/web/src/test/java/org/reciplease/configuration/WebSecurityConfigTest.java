package org.reciplease.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.controller.MeasureController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the permissive local/test security and CORS configuration used outside the
 * {@code cloud} profile (see {@link CloudWebSecurityConfig} for the production equivalent).
 */
@SpringBootTest(
        classes = {WebSecurityConfig.class, WebCorsConfig.class, MeasureController.class, WebSecurityConfigTest.TestController.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@EnableAutoConfiguration
@AutoConfigureMockMvc
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("permits unauthenticated access to the API")
    void permitsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/measures"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("does not require a CSRF token for state-changing requests")
    void csrfIsDisabled() throws Exception {
        mockMvc.perform(post("/api/test"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("does not create a session")
    void sessionsAreStateless() throws Exception {
        mockMvc.perform(get("/api/measures"))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("JSESSIONID"));
    }

    @Test
    @DisplayName("allows the API to be embedded in a frame")
    void frameOptionsAreDisabled() throws Exception {
        mockMvc.perform(get("/api/measures"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Frame-Options"));
    }

    @Test
    @DisplayName("answers CORS preflight requests for the API")
    void corsPreflightIsAllowed() throws Exception {
        mockMvc.perform(options("/api/measures")
                        .header(HttpHeaders.ORIGIN, "http://example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
    }

    @RestController
    static class TestController {
        @PostMapping("/api/test")
        ResponseEntity<Void> create() {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }
}
