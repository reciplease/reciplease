package org.reciplease.repository;

import org.reciplease.model.Invite;

import java.util.Optional;

public interface InviteRepository {
    Optional<Invite> findByCode(String code);

    /**
     * Atomically marks the invite identified by {@code code} as used by {@code userId},
     * succeeding only if it exists and has not already been claimed. Returns the
     * now-claimed invite, or empty if the code is invalid or was already redeemed.
     */
    Optional<Invite> claim(String code, String userId);
}
