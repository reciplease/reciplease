package org.reciplease.controller;

import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.model.User;
import org.reciplease.repository.IdentityConflictException;
import org.reciplease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ReciplaseJwtService.class, MethodSecurityTestSupport.class})
class AuthControllerTest {

    private static final String SECRET = "test-signing-secret-not-for-prod";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ReciplaseJwtService jwtService;

    @Test
    void rejectsAMissingSharedSecret() throws Exception {
        mockMvc.perform(post("/api/auth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "google", "providerId": "google-sub-1"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAWrongSharedSecret() throws Exception {
        mockMvc.perform(post("/api/auth/exchange")
                        .header("X-Internal-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "google", "providerId": "google-sub-1"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsANewUserOnFirstLogin() throws Exception {
        when(userRepository.findByIdentity("google", "google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.createWithIdentity("google", "google-sub-1", "me@gmail.com")).thenReturn(new User("user-1", null));

        mockMvc.perform(post("/api/auth/exchange")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "google", "providerId": "google-sub-1", "email": "me@gmail.com"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", org.hamcrest.Matchers.is("user-1")))
                .andExpect(jsonPath("$.handle").doesNotExist())
                .andExpect(jsonPath("$.token", org.hamcrest.Matchers.notNullValue()));

        verify(userRepository).createWithIdentity("google", "google-sub-1", "me@gmail.com");
    }

    @Test
    void logsInAnExistingUserWithoutCreatingANewOne() throws Exception {
        when(userRepository.findByIdentity("google", "google-sub-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/auth/exchange")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "google", "providerId": "google-sub-1"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", org.hamcrest.Matchers.is("user-1")))
                .andExpect(jsonPath("$.handle", org.hamcrest.Matchers.is("my-handle")));

        verify(userRepository, never()).createWithIdentity(any(), any(), any());
    }

    @Test
    void loggingInAnExistingUserBackfillsTheStoredEmail() throws Exception {
        when(userRepository.findByIdentity("google", "google-sub-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/auth/exchange")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "google", "providerId": "google-sub-1", "email": "me@gmail.com"}"""))
                .andExpect(status().isOk());

        verify(userRepository).updateIdentityEmail("google", "google-sub-1", "me@gmail.com");
    }

    @Test
    void loggingInAnExistingUserWithoutAnEmailDoesNotClobberTheStoredEmail() throws Exception {
        when(userRepository.findByIdentity("google", "google-sub-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/auth/exchange")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "google", "providerId": "google-sub-1"}"""))
                .andExpect(status().isOk());

        verify(userRepository, never()).updateIdentityEmail(any(), any(), any());
    }

    @Test
    void linksANewIdentityWhenAValidLinkTokenIsProvided() throws Exception {
        final var linkToken = jwtService.mint("user-1");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/auth/exchange")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "github", "providerId": "github-sub-1", "linkToken": "%s", "email": "me@github.com"}""".formatted(linkToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", org.hamcrest.Matchers.is("user-1")));

        verify(userRepository).linkIdentity("user-1", "github", "github-sub-1", "me@github.com");
    }

    @Test
    void returnsConflictWhenLinkingAnIdentityAlreadyLinkedToADifferentUser() throws Exception {
        final var linkToken = jwtService.mint("user-1");
        org.mockito.Mockito.doThrow(new IdentityConflictException("github", "github-sub-1"))
                .when(userRepository).linkIdentity("user-1", "github", "github-sub-1", "me@github.com");

        mockMvc.perform(post("/api/auth/exchange")
                        .header("X-Internal-Secret", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"provider": "github", "providerId": "github-sub-1", "linkToken": "%s", "email": "me@github.com"}""".formatted(linkToken)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
    void refreshMintsAFreshTokenForTheAuthenticatedUser() throws Exception {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/auth/refresh").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("user-1")))
                .andExpect(jsonPath("$.handle", is("my-handle")))
                .andExpect(jsonPath("$.token", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
    void refreshedTokenParsesBackToTheSameUser() throws Exception {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        final var response = mockMvc.perform(post("/api/auth/refresh").with(csrf()))
                .andReturn().getResponse().getContentAsString();
        final var token = com.jayway.jsonpath.JsonPath.<String>read(response, "$.token");

        org.assertj.core.api.Assertions.assertThat(jwtService.parse(token)).contains("user-1");
    }

    @Test
    @WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
    void refreshReturnsANullHandleWhenNotSet() throws Exception {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", null)));

        mockMvc.perform(post("/api/auth/refresh").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").doesNotExist());
    }

    @Test
    void refreshRequiresAuthentication() throws Exception {
        // No @WithMockUser here (unlike the tests above) — the security context is empty,
        // matching an unauthenticated caller, distinct from @WithAnonymousUser's populated
        // AnonymousAuthenticationToken (which @PreAuthorize("isAuthenticated()") treats as a
        // 403, not a 401 — see MethodSecurityTestSupport).
        mockMvc.perform(post("/api/auth/refresh").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
