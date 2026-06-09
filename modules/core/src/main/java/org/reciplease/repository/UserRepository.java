package org.reciplease.repository;

import org.reciplease.model.User;

/**
 * Stores authenticated, allowlisted users keyed by their stable Google subject id.
 */
public interface UserRepository {
    User save(User user);
}
