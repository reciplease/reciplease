package org.reciplease.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.controller.MeasureController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the production JWT-based security configuration used in the {@code cloud} profile
 * (see {@link WebSecurityConfig} for the local/test equivalent). The filter chain itself no
 * longer gates anything by URL — it only wires up JWT validation — so the "protected endpoint"
 * tests exercise {@code @PreAuthorize} method security (enabled by
 * {@code @EnableMethodSecurity} on {@link CloudWebSecurityConfig}) via the annotated
 * {@link TestController#create()} below, not a URL-matcher rule.
 */
@SpringBootTest(
        classes = {CloudWebSecurityConfig.class, MeasureController.class, CloudWebSecurityConfigTest.TestController.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("cloud")
class CloudWebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MembershipJwtAuthenticationConverter converter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("permits unauthenticated GET access to public read endpoints")
    void publicGetIsPermitted() throws Exception {
        mockMvc.perform(get("/api/measures"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("rejects unauthenticated requests to a @PreAuthorize-protected endpoint")
    void protectedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("rejects authenticated requests without the role required by @PreAuthorize")
    void protectedEndpointRequiresRole() throws Exception {
        mockMvc.perform(post("/api/test").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("permits requests with the role required by @PreAuthorize")
    void protectedEndpointAllowedWithRole() throws Exception {
        mockMvc.perform(post("/api/test").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECIPLEASE"))))
                .andExpect(status().isCreated());
    }

    @RestController
    static class TestController {
        @PostMapping("/api/test")
        @PreAuthorize("hasRole('RECIPLEASE')")
        ResponseEntity<Void> create() {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }
}
