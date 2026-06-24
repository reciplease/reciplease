package org.reciplease.repository;

import java.util.List;

/** The provider identities linked to a user (provider ids are never exposed). */
public interface UserIdentityRepository {
    List<String> findProvidersForUser(String userId);

    /** Removes the user's identity for {@code provider}, if any. */
    void removeForUser(String userId, String provider);
}
