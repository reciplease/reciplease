package org.reciplease.model;

/**
 * A user's email address, typed so callers can't accidentally pass a raw string
 * (a user id, a house name, ...) where an email is expected.
 */
public record Email(String value) {

    /**
     * Replaces the local part with its first/last two characters and a fixed
     * {@code **} in between (e.g. {@code rhys.saldanha@gmail.com} ->
     * {@code rh**ha@gmail.com}), so a house's other members are identifiable by
     * the first/last letters without exposing the full address. Domain is left
     * untouched — it's not personally identifying on its own.
     */
    public Email masked() {
        final var at = value.indexOf('@');
        final var local = at >= 0 ? value.substring(0, at) : value;
        final var domain = at >= 0 ? value.substring(at) : "";

        final String maskedLocal;
        if (local.length() <= 2) {
            maskedLocal = "**";
        } else if (local.length() <= 4) {
            maskedLocal = local.charAt(0) + "**";
        } else {
            maskedLocal = local.substring(0, 2) + "**" + local.substring(local.length() - 2);
        }

        return new Email(maskedLocal + domain);
    }

    @Override
    public String toString() {
        return value;
    }
}
