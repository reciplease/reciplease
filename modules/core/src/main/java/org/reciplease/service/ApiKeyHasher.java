package org.reciplease.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hashes raw API key secrets for storage; SHA-256 is enough here since keys are high-entropy random values, not passwords. */
final class ApiKeyHasher {

    private ApiKeyHasher() {}

    static String hash(final String rawKey) {
        try {
            final var digest = MessageDigest.getInstance("SHA-256").digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
