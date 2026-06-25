package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Links a login-provider identity to a Reciplease user. {@code id} is the composite
 * key {@code "<provider>:<providerId>"} (e.g. {@code "github:12345"}), which both
 * uniquely identifies the identity and lets Mongo enforce no identity is linked twice.
 */
@Document("user_identities")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityDocument {

    @Id
    private String id;
    private String userId;
    /** The provider account's email at link time, so the user can tell identities apart. May be null. */
    private String email;

    public static String idFor(final String provider, final String providerId) {
        return provider + ":" + providerId;
    }
}
