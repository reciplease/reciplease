package org.reciplease.repository;

import java.util.Optional;

/**
 * Tracks WebAuthn challenges between being issued and consumed, so each one can only be used
 * once and only briefly. The stateless (no {@code HttpSession}) equivalent of the challenge
 * storage the default Spring Security WebAuthn filters rely on.
 * <p>
 * Also remembers the user id the challenge was issued for (the WebAuthn options object itself
 * can't be round-tripped through the client and back — Spring Security's WebAuthn Jackson
 * module has no deserializer for it — so the finish step reconstructs an equivalent options
 * object server-side from just the challenge and this remembered id, rather than trusting
 * anything the client echoes back). Login is usernameless, so it has no id to remember.
 */
public interface WebAuthnChallengeLedger {

    /** Records that {@code challenge} (base64url) has just been issued, for {@code userId}. */
    void issue(String challenge, String userId);

    /**
     * Atomically marks {@code challenge} as used and returns the user id it was issued for,
     * succeeding only the first time for a given challenge (and only within its expiry window).
     * Empty if the challenge is unknown, already used, or expired — callers must reject the
     * request in that case.
     */
    Optional<String> consumeForRegistration(String challenge);

    /** As {@link #consumeForRegistration}, for the login flow, which has no user id to return. */
    boolean consumeForLogin(String challenge);
}
