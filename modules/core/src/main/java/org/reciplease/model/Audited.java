package org.reciplease.model;

/**
 * Marker for domain entities that carry an audit trail. The {@code createdBy} value
 * is the Google subject id of the user who first persisted the entity.
 */
public interface Audited extends Identifiable {
    String createdBy();
}
