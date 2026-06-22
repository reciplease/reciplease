package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.House;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Invite;
import org.reciplease.repository.AllowlistRepository;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.InviteRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class InviteServiceTest {

    private static final String HOUSE_ID = "house-1";

    @Mock
    private InviteRepository inviteRepository;
    @Mock
    private HouseRepository houseRepository;
    @Mock
    private AllowlistRepository allowlistRepository;
    @Mock
    private InviteCodeGenerator inviteCodeGenerator;

    private InviteService inviteService;

    @BeforeEach
    void setUp() {
        inviteService = new InviteService(inviteRepository, houseRepository, allowlistRepository, inviteCodeGenerator);
    }

    @Test
    void acceptClaimsTheCodeAllowlistsTheUserAndAddsThemToTheHouse() {
        var invite = new Invite("invite-1", "code1", HOUSE_ID, HouseRole.READ_ONLY, Instant.now(), null, null);
        var house = new House(HOUSE_ID, "My House", Instant.now());
        when(inviteRepository.claim("code1", "user-1")).thenReturn(Optional.of(invite));
        when(houseRepository.findById(HOUSE_ID)).thenReturn(Optional.of(house));

        var accepted = inviteService.accept("code1", "user-1");

        assertThat(accepted, is(Optional.of(house)));
        verify(allowlistRepository).add("user-1");
        verify(houseRepository).addMember(HOUSE_ID, "user-1", HouseRole.READ_ONLY);
    }

    @Test
    void acceptReturnsEmptyWhenTheCodeIsInvalidOrAlreadyUsed() {
        when(inviteRepository.claim("bad-code", "user-1")).thenReturn(Optional.empty());

        var accepted = inviteService.accept("bad-code", "user-1");

        assertThat(accepted, is(Optional.empty()));
        verify(allowlistRepository, never()).add(any());
    }

    @Test
    void createInviteGeneratesAnAlphanumericCodeAndPersistsTheInvite() {
        when(inviteCodeGenerator.generate()).thenReturn("abcXYZ123");
        when(inviteRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = inviteService.createInvite(HOUSE_ID, HouseRole.READ_ONLY);

        assertThat(created.code(), is("abcXYZ123"));
        assertThat(created.code(), matchesPattern("[A-Za-z0-9]+"));
        assertThat(created.houseId(), is(HOUSE_ID));
        assertThat(created.role(), is(HouseRole.READ_ONLY));
        assertThat(created.usedAt(), is((Instant) null));
    }

    @Test
    void pendingInvitesExcludesAlreadyUsedInvites() {
        var pending = new Invite("invite-1", "code1", HOUSE_ID, HouseRole.OWNER, Instant.now(), null, null);
        var used = new Invite("invite-2", "code2", HOUSE_ID, HouseRole.READ_ONLY, Instant.now(), Instant.now(), "user-1");
        when(inviteRepository.findAllForHouse(HOUSE_ID)).thenReturn(List.of(pending, used));

        var actual = inviteService.pendingInvites(HOUSE_ID);

        assertThat(actual, contains(pending));
    }

    @Test
    void deleteInviteRemovesAnInviteBelongingToTheHouse() {
        var invite = new Invite("invite-1", "code1", HOUSE_ID, HouseRole.OWNER, Instant.now(), null, null);
        when(inviteRepository.findById("invite-1")).thenReturn(Optional.of(invite));

        var deleted = inviteService.deleteInvite(HOUSE_ID, "invite-1");

        assertThat(deleted, is(true));
        verify(inviteRepository).delete("invite-1");
    }

    @Test
    void deleteInviteRefusesToDeleteAnInviteBelongingToAnotherHouse() {
        var invite = new Invite("invite-1", "code1", "other-house", HouseRole.OWNER, Instant.now(), null, null);
        when(inviteRepository.findById("invite-1")).thenReturn(Optional.of(invite));

        var deleted = inviteService.deleteInvite(HOUSE_ID, "invite-1");

        assertThat(deleted, is(false));
        verify(inviteRepository, never()).delete(any());
    }

    @Test
    void deleteInviteReturnsFalseWhenTheInviteDoesNotExist() {
        when(inviteRepository.findById("missing")).thenReturn(Optional.empty());

        var deleted = inviteService.deleteInvite(HOUSE_ID, "missing");

        assertThat(deleted, is(false));
        verify(inviteRepository, never()).delete(any());
    }
}
