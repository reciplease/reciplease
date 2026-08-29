package org.reciplease.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.model.House;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Invite;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.InviteRepository;
import org.reciplease.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InviteController.class)
@Import(MethodSecurityTestSupport.class)
class InviteControllerTest {

    private static final String HOUSE_ID = "house-1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InviteRepository inviteRepository;

    @MockitoBean
    private HouseRepository houseRepository;

    @MockitoBean
    private InviteService inviteService;

    @Test
    void previewReturnsTheHouseForAnUnusedInvite() throws Exception {
        var invite = new Invite(
                "invite-1", "code10000000000000000000", HOUSE_ID, HouseRole.READ_ONLY, Instant.now(), null, null);
        var house = new House(HOUSE_ID, "My House", Instant.now());
        when(inviteRepository.findByCode("code10000000000000000000")).thenReturn(Optional.of(invite));
        when(houseRepository.findById(HOUSE_ID)).thenReturn(Optional.of(house));

        mockMvc.perform(get("/api/invites/code10000000000000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houseName", org.hamcrest.Matchers.is("My House")));
    }

    @Test
    void previewReturnsNotFoundForAnAlreadyUsedInvite() throws Exception {
        var invite = new Invite(
                "invite-1",
                "code10000000000000000000",
                HOUSE_ID,
                HouseRole.READ_ONLY,
                Instant.now(),
                Instant.now(),
                "user-1");
        when(inviteRepository.findByCode("code10000000000000000000")).thenReturn(Optional.of(invite));

        mockMvc.perform(get("/api/invites/code10000000000000000000")).andExpect(status().isNotFound());
    }

    @Test
    void previewReturnsNotFoundForAnUnknownCode() throws Exception {
        when(inviteRepository.findByCode("missing00000000000000000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/invites/missing00000000000000000")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user-1")
    void acceptReturnsTheHouseWhenTheInviteIsRedeemed() throws Exception {
        var house = new House(HOUSE_ID, "My House", Instant.now());
        when(inviteService.accept("code10000000000000000000", "user-1")).thenReturn(Optional.of(house));

        mockMvc.perform(post("/api/invites/code10000000000000000000/accept").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", org.hamcrest.Matchers.is("My House")));
    }

    @Test
    @WithMockUser(username = "user-1")
    void acceptReturnsNotFoundWhenTheCodeIsInvalidOrAlreadyUsed() throws Exception {
        when(inviteService.accept("badcode00000000000000000", "user-1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/invites/badcode00000000000000000/accept").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @org.springframework.security.test.context.support.WithAnonymousUser
    void acceptIsRejectedForAnUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/invites/code10000000000000000000/accept").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void previewRejectsAMalformedCode() throws Exception {
        mockMvc.perform(get("/api/invites/short")).andExpect(status().isBadRequest());
    }

    @Test
    void previewRejectsACodeContainingNonAlphanumericCharacters() throws Exception {
        mockMvc.perform(get("/api/invites/has-a-dash-in-it-badcode")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user-1")
    void acceptRejectsAMalformedCode() throws Exception {
        mockMvc.perform(post("/api/invites/short/accept").with(csrf())).andExpect(status().isBadRequest());
    }
}
