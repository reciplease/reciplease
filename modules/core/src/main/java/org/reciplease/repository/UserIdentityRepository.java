package org.reciplease.repository;

import java.util.List;

/** Read access to the provider names linked to a user (provider ids are never exposed). */
public interface UserIdentityRepository {
    List<String> findProvidersForUser(String userId);
}
