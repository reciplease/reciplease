package org.reciplease.controller;

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

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        var invite = new Invite("invite-1", "code1", HOUSE_ID, HouseRole.READ_ONLY, Instant.now(), null, null);
        var house = new House(HOUSE_ID, "My House", Instant.now());
        when(inviteRepository.findByCode("code1")).thenReturn(Optional.of(invite));
        when(houseRepository.findById(HOUSE_ID)).thenReturn(Optional.of(house));

        mockMvc.perform(get("/api/invites/code1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houseName", org.hamcrest.Matchers.is("My House")));
    }

    @Test
    void previewReturnsNotFoundForAnAlreadyUsedInvite() throws Exception {
        var invite = new Invite("invite-1", "code1", HOUSE_ID, HouseRole.READ_ONLY, Instant.now(), Instant.now(), "user-1");
        when(inviteRepository.findByCode("code1")).thenReturn(Optional.of(invite));

        mockMvc.perform(get("/api/invites/code1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void previewReturnsNotFoundForAnUnknownCode() throws Exception {
        when(inviteRepository.findByCode("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/invites/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user-1")
    void acceptReturnsTheHouseWhenTheInviteIsRedeemed() throws Exception {
        var house = new House(HOUSE_ID, "My House", Instant.now());
        when(inviteService.accept("code1", "user-1")).thenReturn(Optional.of(house));

        mockMvc.perform(post("/api/invites/code1/accept").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", org.hamcrest.Matchers.is("My House")));
    }

    @Test
    @WithMockUser(username = "user-1")
    void acceptReturnsNotFoundWhenTheCodeIsInvalidOrAlreadyUsed() throws Exception {
        when(inviteService.accept("bad-code", "user-1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/invites/bad-code/accept").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @org.springframework.security.test.context.support.WithAnonymousUser
    void acceptIsRejectedForAnUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/invites/code1/accept").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
