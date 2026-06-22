package org.reciplease.repository;

import org.reciplease.model.User;

import java.util.Optional;

/**
 * Stores authenticated, allowlisted users and the provider identities linked to them.
 */
public interface UserRepository {
    User save(User user);

    Optional<User> findById(String id);

    /** Looks up the user linked to {@code (provider, providerId)}, if any. */
    Optional<User> findByIdentity(String provider, String providerId);

    /** Creates a brand-new user (with a {@code null} handle) and links the given identity to it. */
    User createWithIdentity(String provider, String providerId);

    /**
     * Links {@code (provider, providerId)} to {@code userId}. A no-op if already linked to
     * {@code userId}; throws {@link IdentityConflictException} if linked to a different user.
     */
    void linkIdentity(String userId, String provider, String providerId);

    /** Sets {@code userId}'s handle; throws {@link HandleTakenException} if already in use by another user. */
    void setHandle(String userId, String handle);
}
