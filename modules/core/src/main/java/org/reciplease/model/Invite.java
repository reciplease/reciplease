package org.reciplease.model;

import java.time.Instant;

/**
 * A one-time code granting the redeemer a {@link HouseRole} in {@code houseId}.
 * {@code usedAt}/{@code usedByUserId} are null until the invite is claimed.
 */
public record Invite(
        String id, String code, String houseId, HouseRole role, Instant createdAt, Instant usedAt, String usedByUserId)
        implements Identifiable {}
