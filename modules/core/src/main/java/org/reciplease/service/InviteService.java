package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.House;
import org.reciplease.repository.AllowlistRepository;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.InviteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final HouseRepository houseRepository;
    private final AllowlistRepository allowlistRepository;

    /**
     * Redeems a one-time invite code: claims it (atomically, so it can't be reused),
     * allowlists the redeemer's email, and grants them the invite's role in its house.
     * Returns empty if the code is invalid or was already redeemed.
     */
    public Optional<House> accept(final String code, final String userId, final String email) {
        return inviteRepository.claim(code, userId).map(invite -> {
            allowlistRepository.add(email);
            houseRepository.addMember(invite.houseId(), userId, invite.role());
            return houseRepository.findById(invite.houseId())
                    .orElseThrow(() -> new IllegalStateException("Invite references a missing house: " + invite.houseId()));
        });
    }
}
