package org.reciplease.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.model.User;
import org.reciplease.repository.IdentityConflictException;
import org.reciplease.repository.UserRepository;
import org.reciplease.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ReciplaseJwtService jwtService;

    @BeforeEach
    void stubRefreshTokenIssuance() {
        when(refreshTokenService.issue(any())).thenReturn(
                new RefreshTokenService.IssuedRefreshToken("issued-raw-refresh-token", Instant.parse("2026-02-01T00:00:00Z")));
    }

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
                .andExpect(jsonPath("$.userId", is("user-1")))
                .andExpect(jsonPath("$.handle").doesNotExist())
                .andExpect(jsonPath("$.token", org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.refreshToken", is("issued-raw-refresh-token")));

        verify(userRepository).createWithIdentity("google", "google-sub-1", "me@gmail.com");
        verify(refreshTokenService).issue("user-1");
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
                .andExpect(jsonPath("$.userId", is("user-1")))
                .andExpect(jsonPath("$.handle", is("my-handle")))
                .andExpect(jsonPath("$.refreshToken", is("issued-raw-refresh-token")));

        verify(userRepository, never()).createWithIdentity(any(), any(), any());
        verify(refreshTokenService).issue("user-1");
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
                .andExpect(jsonPath("$.userId", is("user-1")))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());

        verify(userRepository).linkIdentity("user-1", "github", "github-sub-1", "me@github.com");
        verify(refreshTokenService, never()).issue(any());
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
    void refreshWithNoCookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());

        verify(refreshTokenService, never()).rotate(any());
    }

    @Test
    void refreshWithAValidCookieRotatesAndReturnsAFreshExchangeResponse() throws Exception {
        when(refreshTokenService.rotate("valid-refresh-cookie")).thenReturn(
                new RefreshTokenService.Rotated("user-1", "rotated-raw-refresh-token", Instant.parse("2026-03-01T00:00:00Z")));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("reciplease-refresh", "valid-refresh-cookie")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("user-1")))
                .andExpect(jsonPath("$.handle", is("my-handle")))
                .andExpect(jsonPath("$.token", org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.refreshToken", is("rotated-raw-refresh-token")));

        verify(refreshTokenService).rotate("valid-refresh-cookie");
    }

    @Test
    void refreshWithReuseDetectedIsUnauthorized() throws Exception {
        when(refreshTokenService.rotate("reused-refresh-cookie")).thenReturn(new RefreshTokenService.ReuseDetected("user-1"));

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("reciplease-refresh", "reused-refresh-cookie")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithAnInvalidCookieIsUnauthorized() throws Exception {
        when(refreshTokenService.rotate("bad-refresh-cookie")).thenReturn(new RefreshTokenService.Invalid());

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("reciplease-refresh", "bad-refresh-cookie")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void issueRefreshTokenRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token"))
                .andExpect(status().isUnauthorized());

        verify(refreshTokenService, never()).issue(any());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
    void issueRefreshTokenBackfillsOneForTheAuthenticatedUser() throws Exception {
        when(refreshTokenService.issue("user-1")).thenReturn(
                new RefreshTokenService.IssuedRefreshToken("backfilled-raw-refresh-token", Instant.parse("2026-03-01T00:00:00Z")));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(post("/api/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("user-1")))
                .andExpect(jsonPath("$.handle", is("my-handle")))
                .andExpect(jsonPath("$.token", org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.refreshToken", is("backfilled-raw-refresh-token")));

        verify(refreshTokenService).issue("user-1");
    }

    @Test
    void logoutWithCookiePresentRevokesTheRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout").cookie(new jakarta.servlet.http.Cookie("reciplease-refresh", "some-refresh-cookie")))
                .andExpect(status().isOk());

        verify(refreshTokenService).revokeByRawToken("some-refresh-cookie");
    }

    @Test
    void logoutWithoutCookieDoesNotAttemptToRevokeAnything() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());

        verify(refreshTokenService, never()).revokeByRawToken(any());
    }
}
