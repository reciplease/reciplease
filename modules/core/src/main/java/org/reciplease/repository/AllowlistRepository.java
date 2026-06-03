package org.reciplease.repository;

import java.util.List;

/**
 * Stores the set of Google account emails that are permitted to access Reciplease.
 */
public interface AllowlistRepository {
    boolean contains(String email);

    List<String> findAll();

    void add(String email);

    void remove(String email);
}
