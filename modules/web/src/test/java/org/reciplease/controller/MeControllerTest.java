package org.reciplease.controller;

import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.model.User;
import org.reciplease.repository.HandleTakenException;
import org.reciplease.repository.UserIdentityRepository;
import org.reciplease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@WithMockUser(username = "user-1", authorities = "ROLE_RECIPLEASE")
@Import(MethodSecurityTestSupport.class)
class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserIdentityRepository userIdentityRepository;

    @Test
    void meReturnsTheCurrentUsersIdAndHandle() throws Exception {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", "my-handle")));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("user-1")))
                .andExpect(jsonPath("$.handle", is("my-handle")));
    }

    @Test
    void meReturnsANullHandleWhenNotSet() throws Exception {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(new User("user-1", null)));

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle", nullValue()));
    }

    @Test
    void identitiesReturnsOnlyProviderNames() throws Exception {
        when(userIdentityRepository.findProvidersForUser("user-1")).thenReturn(List.of("google", "github"));

        mockMvc.perform(get("/api/me/identities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providers", org.hamcrest.Matchers.contains("google", "github")));
    }

    @Test
    void setHandleTrimsAndPersistsAValidHandle() throws Exception {
        mockMvc.perform(post("/api/me/handle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle": "  my-new-handle  "}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("user-1")))
                .andExpect(jsonPath("$.handle", is("my-new-handle")));

        verify(userRepository).setHandle("user-1", "my-new-handle");
    }

    @Test
    void setHandleRejectsABlankHandle() throws Exception {
        mockMvc.perform(post("/api/me/handle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle": "   "}"""))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).setHandle(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void setHandleRejectsAHandleOverThirtyCodePoints() throws Exception {
        final var tooLong = "a".repeat(31);

        mockMvc.perform(post("/api/me/handle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle": "%s"}""".formatted(tooLong)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setHandleAcceptsThirtyCodePointsExactly() throws Exception {
        final var exactly30 = "a".repeat(30);

        mockMvc.perform(post("/api/me/handle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle": "%s"}""".formatted(exactly30)))
                .andExpect(status().isOk());
    }

    @Test
    void setHandleCountsByCodePointNotUtf16CharsForSurrogatePairEmoji() throws Exception {
        // U+1F600 (the "grinning face" emoji) is one code point but two UTF-16 chars,
        // repeated 30 times: 30 code points (valid), 60 chars (would be rejected if length()
        // were used instead of codePointCount()).
        final var thirtyEmoji = "😀".repeat(30);

        mockMvc.perform(post("/api/me/handle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle": "%s"}""".formatted(thirtyEmoji)))
                .andExpect(status().isOk());
    }

    @Test
    void setHandleRejectsThirtyOneEmojiCodePoints() throws Exception {
        final var thirtyOneEmoji = "😀".repeat(31);

        mockMvc.perform(post("/api/me/handle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle": "%s"}""".formatted(thirtyOneEmoji)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setHandleReturnsConflictWhenAnotherUserAlreadyHasThatHandle() throws Exception {
        doThrow(new HandleTakenException("taken-handle")).when(userRepository).setHandle("user-1", "taken-handle");

        mockMvc.perform(post("/api/me/handle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"handle": "taken-handle"}"""))
                .andExpect(status().isConflict());
    }
}
