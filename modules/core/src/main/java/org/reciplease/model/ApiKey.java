package org.reciplease.model;

import java.time.Instant;

/**
 * A long-lived service-account credential scoped to one house, for third-party clients (e.g. a
 * Home Assistant integration) that can't do an interactive sign-in. Created by a house owner,
 * who names it and picks the {@link HouseRole} it acts with — it is not tied to any individual
 * user's own access, so revoking a user's house membership does not affect keys they created.
 * {@code keyHash} is the SHA-256 digest of the raw secret — the raw value itself is never
 * persisted, only returned once at creation. {@code keyPrefix} is a short, non-secret slice of
 * the raw key used to look up the matching record without scanning every stored hash.
 */
public record ApiKey(
        String id,
        String houseId,
        String name,
        HouseRole role,
        String createdByUserId,
        String keyPrefix,
        String keyHash,
        Instant createdAt,
        Instant lastUsedAt) implements Identifiable, HouseScoped {
}
