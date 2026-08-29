package org.reciplease.repository;

import java.util.List;
import java.util.Optional;
import org.reciplease.model.Invite;

public interface InviteRepository {
    Optional<Invite> findByCode(String code);

    /**
     * Atomically marks the invite identified by {@code code} as used by {@code userId},
     * succeeding only if it exists and has not already been claimed. Returns the
     * now-claimed invite, or empty if the code is invalid or was already redeemed.
     */
    Optional<Invite> claim(String code, String userId);

    /** Persists a brand-new, never-yet-used invite. */
    Invite create(Invite invite);

    List<Invite> findAllForHouse(String houseId);

    Optional<Invite> findById(String id);

    void delete(String id);
}
