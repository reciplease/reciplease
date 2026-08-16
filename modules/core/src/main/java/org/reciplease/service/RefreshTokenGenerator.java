package org.reciplease.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates raw refresh token secrets. Unlike {@link ApiKeyGenerator}, no {@code rcpl_}-style
 * prefix marker is needed — this token is never sent as a bearer token through the resource
 * server filter chain, only read via {@code @CookieValue} at the controller layer, so nothing
 * needs to route on a prefix to distinguish it from anything else.
 */
@Component
public class RefreshTokenGenerator {

    private static final int SECRET_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        final var bytes = new byte[SECRET_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
