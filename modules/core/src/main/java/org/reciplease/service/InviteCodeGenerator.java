package org.reciplease.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates one-time invite codes. Alphanumeric only (no {@code -}/{@code _}/other
 * punctuation) so codes are unambiguous wherever they're copied or read aloud.
 */
@Component
public class InviteCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 24;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        final var code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
