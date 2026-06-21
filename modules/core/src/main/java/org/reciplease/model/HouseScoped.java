package org.reciplease.model;

/**
 * Marker for domain entities that belong to a house and are only visible/writable within it
 * (modulo any entity-specific public/private override, e.g. {@link Recipe#isPublic()}). Mirrors
 * {@link Audited} — a small, composable contract rather than a field every model duplicates.
 */
public interface HouseScoped {
    String houseId();
}
