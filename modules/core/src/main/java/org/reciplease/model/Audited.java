package org.reciplease.model;

import java.time.Instant;

/**
 * Marker for domain entities that carry an audit trail. The {@code createdBy} value
 * is the internal {@link User#id()} of the user who first persisted the entity.
 * {@code createdAt} and {@code updatedAt} are set by Spring Data auditing on first save
 * and every save respectively.
 */
public interface Audited extends Identifiable {
    String createdBy();

    Instant createdAt();

    Instant updatedAt();
}
