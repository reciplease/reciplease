package org.reciplease.model;

import java.time.Instant;

/**
 * A household whose recipes, pantry, and planned recipes are kept separate from
 * other houses. Membership and roles are not modeled here — they are a relationship
 * queried and mutated through {@link org.reciplease.repository.HouseRepository},
 * not an attribute of the house itself.
 */
public record House(String id, String name, Instant createdAt) implements Identifiable {
}
