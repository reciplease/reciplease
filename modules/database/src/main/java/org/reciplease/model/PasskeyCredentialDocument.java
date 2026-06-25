package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A registered passkey credential. {@code id} is the credential id (base64url), which both
 * uniquely identifies the credential and lets Mongo enforce it's never stored twice.
 */
@Document("passkey_credentials")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyCredentialDocument {

    @Id
    private String id;
    private String userId;
    private String publicKeyCose;
    private long signatureCount;
    private boolean uvInitialized;
    private Set<String> transports;
    private boolean backupEligible;
    private boolean backupState;
    private String label;
    @CreatedDate
    private Instant createdAt;

    public static PasskeyCredentialDocument from(final CredentialRecord credential, final String userId, final String label) {
        return PasskeyCredentialDocument.builder()
                .id(credential.getCredentialId().toBase64UrlString())
                .userId(userId)
                .publicKeyCose(new Bytes(credential.getPublicKey().getBytes()).toBase64UrlString())
                .signatureCount(credential.getSignatureCount())
                .uvInitialized(credential.isUvInitialized())
                .transports(credential.getTransports().stream().map(AuthenticatorTransport::getValue).collect(Collectors.toSet()))
                .backupEligible(credential.isBackupEligible())
                .backupState(credential.isBackupState())
                .label(label)
                .build();
    }

    public CredentialRecord toCredentialRecord() {
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(Bytes.fromBase64(id))
                .userEntityUserId(PasskeyUserHandles.toHandle(userId))
                .publicKey(ImmutablePublicKeyCose.fromBase64(publicKeyCose))
                .signatureCount(signatureCount)
                .uvInitialized(uvInitialized)
                .transports(transports.stream().map(AuthenticatorTransport::valueOf).collect(Collectors.toSet()))
                .backupEligible(backupEligible)
                .backupState(backupState)
                .label(label)
                .created(createdAt)
                .lastUsed(createdAt)
                .build();
    }
}
