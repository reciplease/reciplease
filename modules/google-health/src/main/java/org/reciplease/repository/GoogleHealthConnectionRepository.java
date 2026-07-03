package org.reciplease.repository;

import org.reciplease.model.GoogleHealthConnection;

import java.util.Optional;

/** Stores each user's linked Google Health account, keyed one-to-one by Reciplease {@code userId}. */
public interface GoogleHealthConnectionRepository {
    Optional<GoogleHealthConnection> findByUserId(String userId);

    /** Inserts or overwrites the connection for {@code connection.userId()}. */
    GoogleHealthConnection save(GoogleHealthConnection connection);

    void deleteByUserId(String userId);
}
