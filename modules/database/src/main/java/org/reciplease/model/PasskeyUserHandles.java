package org.reciplease.model;

import org.springframework.security.web.webauthn.api.Bytes;

import java.nio.charset.StandardCharsets;

/**
 * WebAuthn identifies a user by an opaque "user handle" ({@link Bytes}), distinct from any
 * Reciplease concept. Rather than maintaining a second id and a lookup table between the two,
 * the handle is just the Reciplease user id's UTF-8 bytes — deterministic in both directions,
 * so no separate user-entity storage is needed at all.
 */
public final class PasskeyUserHandles {

    private PasskeyUserHandles() {
    }

    public static Bytes toHandle(final String userId) {
        return new Bytes(userId.getBytes(StandardCharsets.UTF_8));
    }

    public static String toUserId(final Bytes handle) {
        return new String(handle.getBytes(), StandardCharsets.UTF_8);
    }
}
