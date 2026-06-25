package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Tracks a WebAuthn challenge between being issued (a creation/request-options call) and
 * consumed (the matching finish call), so it can be used exactly once and only briefly — the
 * stateless equivalent of the {@code HttpSession}-backed challenge storage the default
 * Spring Security WebAuthn filters use, which this app can't rely on (see {@code PasskeyController}).
 * {@code id} is the challenge itself (base64url), so issuing the same challenge twice is
 * naturally impossible. Expires automatically via the TTL index on {@code createdAt}.
 */
@Document("webauthn_challenges")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthnChallengeDocument {

    @Id
    private String id;
    /** The user id the challenge was issued for; null for the usernameless login flow. */
    private String userId;
    @Indexed(expireAfter = "5m")
    @CreatedDate
    private Instant createdAt;
}
