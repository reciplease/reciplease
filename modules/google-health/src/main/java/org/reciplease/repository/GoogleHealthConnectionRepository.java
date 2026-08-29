package org.reciplease.repository;

import java.util.Optional;
import org.reciplease.model.GoogleHealthConnection;

/** Stores each user's linked Google Health account, keyed one-to-one by Reciplease {@code userId}. */
public interface GoogleHealthConnectionRepository {
    Optional<GoogleHealthConnection> findByUserId(String userId);

    /** Inserts or overwrites the connection for {@code connection.userId()}. */
    GoogleHealthConnection save(GoogleHealthConnection connection);

    void deleteByUserId(String userId);
}
