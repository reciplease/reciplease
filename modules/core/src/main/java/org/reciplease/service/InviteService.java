package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.House;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Invite;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.InviteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final HouseRepository houseRepository;
    private final InviteCodeGenerator inviteCodeGenerator;

    /**
     * Redeems a one-time invite code: claims it (atomically, so it can't be reused) and grants
     * the redeemer the invite's role in its house. House membership is itself the access grant —
     * there is no separate allowlist. Returns empty if the code is invalid or already redeemed.
     */
    public Optional<House> accept(final String code, final String userId) {
        return inviteRepository.claim(code, userId).map(invite -> {
            houseRepository.addMember(invite.houseId(), userId, invite.role());
            return houseRepository.findById(invite.houseId())
                    .orElseThrow(() -> new IllegalStateException("Invite references a missing house: " + invite.houseId()));
        });
    }

    /** Creates and persists a new one-time invite for {@code houseId} granting {@code role}. */
    public Invite createInvite(final String houseId, final HouseRole role) {
        final var invite = new Invite(null, inviteCodeGenerator.generate(), houseId, role, Instant.now(), null, null);
        return inviteRepository.create(invite);
    }

    /** All not-yet-redeemed invites for {@code houseId}. */
    public List<Invite> pendingInvites(final String houseId) {
        return inviteRepository.findAllForHouse(houseId).stream()
                .filter(invite -> invite.usedAt() == null)
                .collect(toList());
    }

    /**
     * Deletes the pending invite {@code inviteId}, but only if it actually belongs to
     * {@code houseId} — mirrors the {@code belongsToHeaderHouse} defense used elsewhere
     * so a house owner can't delete another house's invite by guessing its id.
     * Returns false if the invite doesn't exist or belongs to a different house.
     */
    public boolean deleteInvite(final String houseId, final String inviteId) {
        final var belongsToHouse = inviteRepository.findById(inviteId)
                .filter(invite -> invite.houseId().equals(houseId))
                .isPresent();
        if (belongsToHouse) {
            inviteRepository.delete(inviteId);
        }
        return belongsToHouse;
    }
}
