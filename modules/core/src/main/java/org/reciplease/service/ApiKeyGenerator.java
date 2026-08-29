package org.reciplease.service;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * Generates raw API key secrets. The {@code rcpl_} prefix distinguishes them from Reciplease's
 * session JWTs at a glance, which lets callers route an incoming bearer token to the right
 * authentication path without first trying to parse it as a JWT.
 */
@Component
public class ApiKeyGenerator {

    public static final String PREFIX = "rcpl_";
    private static final int SECRET_BYTES = 30;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        final var bytes = new byte[SECRET_BYTES];
        random.nextBytes(bytes);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
