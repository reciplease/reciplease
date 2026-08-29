package org.reciplease.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.MethodSecurityTestSupport;
import org.reciplease.configuration.WithHouseMember;
import org.reciplease.configuration.WithHouseOwner;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Invite;
import org.reciplease.repository.HouseRepository;
import org.reciplease.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HouseController.class)
@WithHouseOwner
@Import(MethodSecurityTestSupport.class)
class HouseControllerTest {

    private static final String HOUSE_ID = "house-1";

    @MockitoBean
    private HouseRepository houseRepository;

    @MockitoBean
    private InviteService inviteService;

    @MockitoBean(name = "houseAccess")
    private HouseAccess houseAccess;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void stubHouseAccess() {
        when(houseAccess.isOwner()).thenReturn(true);
        when(houseAccess.isMember()).thenReturn(true);
        when(houseAccess.requireHouseId()).thenReturn(HOUSE_ID);
    }

    @Test
    @DisplayName("should return members with their handles")
    void findMembers() throws Exception {
        when(houseRepository.members(HOUSE_ID))
                .thenReturn(List.of(
                        new HouseMembership("owner-id", "owner-handle", HouseRole.OWNER),
                        new HouseMembership("member-id", "member-handle", HouseRole.READ_ONLY)));

        mockMvc.perform(get("/api/houses/members"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"userId": "owner-id", "handle": "owner-handle", "role": "OWNER"},
                          {"userId": "member-id", "handle": "member-handle", "role": "READ_ONLY"}
                        ]""", true));
    }

    @Test
    @DisplayName("returns a null handle for members who haven't set one")
    @WithMockUser(username = "owner-id", authorities = "ROLE_RECIPLEASE")
    void findMembersWithNullHandle() throws Exception {
        when(houseRepository.members(HOUSE_ID))
                .thenReturn(List.of(
                        new HouseMembership("owner-id", null, HouseRole.OWNER),
                        new HouseMembership("member-id", "member-handle", HouseRole.READ_ONLY)));

        mockMvc.perform(get("/api/houses/members"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"userId": "owner-id", "role": "OWNER"},
                          {"userId": "member-id", "handle": "member-handle", "role": "READ_ONLY"}
                        ]""", true));
    }

    @Test
    @DisplayName("findMembers is reachable by read-only members")
    @WithHouseMember
    void findMembersAllowedForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);
        when(houseRepository.members(HOUSE_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/houses/members")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("should update a member's role")
    void updateMemberRole() throws Exception {
        when(houseRepository.members(HOUSE_ID))
                .thenReturn(List.of(new HouseMembership("member-id", "member-handle", HouseRole.OWNER)));

        mockMvc.perform(patch("/api/houses/members/{userId}", "member-id")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "OWNER"}"""))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"userId": "member-id", "handle": "member-handle", "role": "OWNER"}
                        ]""", true));

        verify(houseRepository).addMember(HOUSE_ID, "member-id", HouseRole.OWNER);
    }

    @Test
    @DisplayName("updateMemberRole is forbidden for read-only members")
    @WithHouseMember
    void updateMemberRoleForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(patch("/api/houses/members/{userId}", "member-id")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "OWNER"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should remove a member and return the updated list")
    void removeMember() throws Exception {
        when(houseRepository.members(HOUSE_ID))
                .thenReturn(List.of(new HouseMembership("user", "owner-handle", HouseRole.OWNER)));

        mockMvc.perform(delete("/api/houses/members/{userId}", "member-id").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"userId": "user", "handle": "owner-handle", "role": "OWNER"}
                        ]""", true));

        verify(houseRepository).removeMember(HOUSE_ID, "member-id");
    }

    @Test
    @DisplayName("removing yourself is rejected (can't orphan the house)")
    void removeSelfIsRejected() throws Exception {
        mockMvc.perform(delete("/api/houses/members/{userId}", "user").with(csrf()))
                .andExpect(status().isBadRequest());

        verify(houseRepository, never()).removeMember(any(), any());
    }

    @Test
    @DisplayName("removeMember is forbidden for read-only members")
    @WithHouseMember
    void removeMemberForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(delete("/api/houses/members/{userId}", "member-id").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return pending invites")
    void findPendingInvites() throws Exception {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(inviteService.pendingInvites(HOUSE_ID))
                .thenReturn(List.of(
                        new Invite("invite-1", "abc123", HOUSE_ID, HouseRole.READ_ONLY, createdAt, null, null)));

        mockMvc.perform(get("/api/houses/invites"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"id": "invite-1", "code": "abc123", "role": "READ_ONLY", "createdAt": "2026-01-01T00:00:00Z"}
                        ]""", true));
    }

    @Test
    @DisplayName("findPendingInvites is forbidden for read-only members")
    @WithHouseMember
    void findPendingInvitesForbiddenForReadOnly() throws Exception {
        when(houseAccess.isOwner()).thenReturn(false);

        mockMvc.perform(get("/api/houses/invites")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should create an invite")
    void createInvite() throws Exception {
        var createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(inviteService.createInvite(HOUSE_ID, HouseRole.READ_ONLY))
                .thenReturn(new Invite("invite-1", "abc123", HOUSE_ID, HouseRole.READ_ONLY, createdAt, null, null));

        mockMvc.perform(post("/api/houses/invites")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "READ_ONLY"}"""))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {"id": "invite-1", "code": "abc123", "role": "READ_ONLY", "createdAt": "2026-01-01T00:00:00Z"}""", true));
    }

    @Test
    @DisplayName("should delete a pending invite")
    void deleteInvite() throws Exception {
        when(inviteService.deleteInvite(HOUSE_ID, "invite-1")).thenReturn(true);

        mockMvc.perform(delete("/api/houses/invites/{inviteId}", "invite-1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should 404 when deleting an unknown or mismatched invite")
    void deleteInviteNotFound() throws Exception {
        when(inviteService.deleteInvite(HOUSE_ID, "invite-1")).thenReturn(false);

        mockMvc.perform(delete("/api/houses/invites/{inviteId}", "invite-1").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
