package org.reciplease.repository;

import org.reciplease.model.FitbitConnection;

import java.util.Optional;

/** Stores each user's linked Fitbit account, keyed one-to-one by Reciplease {@code userId}. */
public interface FitbitConnectionRepository {
    Optional<FitbitConnection> findByUserId(String userId);

    /** Inserts or overwrites the connection for {@code connection.userId()}. */
    FitbitConnection save(FitbitConnection connection);

    void deleteByUserId(String userId);
}
